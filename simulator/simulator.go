package main

import (
	"context"
	"encoding/json"
	"log"
	"math/rand"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

// Machine matches your Java Entity structure
type Machine struct {
	ID                    string   `json:"id"`
	Name                  string   `json:"name"`
	Vibration             float64  `json:"vibration"`
	Temperature           float64  `json:"temperature"`
	CurrentPercentOfRated float64  `json:"currentPercentOfRated"`
	Rpm                   float64  `json:"rpm"`
	Status                string   `json:"status"` // Enum as String: NORMAL, RISKY, CRITICAL
	Context               string   `json:"context"`
	LastUpdated           string   `json:"lastUpdated"` // ISO8601 string
	OverloadTripCount     int      `json:"overloadTripCount"`
	Recommendations       []string `json:"recommendations"`
}

// UserCommand represents the manual action sent from the Frontend/Backend
type UserCommand struct {
	Action string `json:"action"` // e.g., "ACTIVATE_COOLING", "STOP", "RESET"
}

func main() {
	// 1. Connect to RabbitMQ
	conn, err := amqp.Dial("amqp://guest:guest@localhost:5672/")
	failOnError(err, "Failed to connect to RabbitMQ")
	defer conn.Close()

	ch, err := conn.Channel()
	failOnError(err, "Failed to open a channel")
	defer ch.Close()

	// 2. Declare the Telemetry Queue (where machines send data)
	qTelemetry, err := ch.QueueDeclare("machine_telemetry", true, false, false, false, nil)
	failOnError(err, "Failed to declare telemetry queue")

	// 3. Start simulating multiple machines
	// We use different IDs that should exist in your Database
	go simulateMachine(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A", 84.0, 5.0)
	// Change ID later to actually be one of the machines in the DB
	go simulateMachine(ch, qTelemetry.Name, "M2", "Compressor B", 60.0, 8.5) // This one starts with high vibration

	// Keep the main thread alive
	log.Printf(" [*] Simulation started. Sending telemetry to RabbitMQ...")
	select {}
}

func simulateMachine(ch *amqp.Channel, queueName string, id string, name string, startTemp float64, startVib float64) {
	// Internal state of the simulation
	currentTemp := startTemp
	currentVib := startVib
	isCoolingActive := false
	isStopped := false

	// --- COMMAND LISTENER ---
	// Create a unique command queue for THIS specific machine
	cmdQueueName := "commands." + id
	cmdQ, _ := ch.QueueDeclare(cmdQueueName, false, false, false, false, nil)
	msgs, _ := ch.Consume(cmdQ.Name, "", true, false, false, false, nil)

	go func() {
		for d := range msgs {
			var cmd UserCommand
			json.Unmarshal(d.Body, &cmd)
			log.Printf("[%s] RECEIVED COMMAND: %s", name, cmd.Action)

			switch cmd.Action {
			case "ACTIVATE_COOLING":
				isCoolingActive = true
			case "STOP":
				isStopped = true
			case "RESET":
				currentTemp = startTemp
				isCoolingActive = false
				isStopped = false
			}
		}
	}()

	// --- TELEMETRY LOOP ---
	for {
		if !isStopped {
			// Simulating physics logic
			if isCoolingActive {
				currentTemp -= 1.5 // Cooling helps drop temperature
				if currentTemp < 50 {
					isCoolingActive = false
				} // Stop cooling at 50
			} else {
				currentTemp += rand.Float64() * 1.2 // Gradually warms up
			}

			// Add some random noise to vibration
			currentVib += (rand.Float64() - 0.5)
		} else {
			// Machine is stopped, values go to zero
			currentTemp = currentTemp * 0.95
			currentVib = 0
		}

		// Create the DTO
		m := Machine{
			ID:                    id,
			Name:                  name,
			Temperature:           currentTemp,
			Vibration:             currentVib,
			CurrentPercentOfRated: 95.0,
			Rpm:                   1500.0,
			Status:                "NORMAL", // Backend logic will change this
			Context:               "NORMAL", //POST_MAINTENANCE
			LastUpdated:           time.Now().UTC().Format(time.RFC3339),
			OverloadTripCount:     0,
			Recommendations:       []string{},
		}

		// Send to RabbitMQ
		body, _ := json.Marshal(m)
		ch.PublishWithContext(context.Background(), "", queueName, false, false, amqp.Publishing{
			ContentType: "application/json",
			Body:        body,
		})

		time.Sleep(3 * time.Second) // Send every 3 seconds
	}
}

func failOnError(err error, msg string) {
	if err != nil {
		log.Fatalf("%s: %s", msg, err)
	}
}
