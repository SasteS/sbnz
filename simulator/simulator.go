package main

import (
	"context"
	"encoding/json"
	"log"
	"math/rand"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

// --- MODELS ---

type Machine struct {
	ID                    string   `json:"id"`
	Name                  string   `json:"name"`
	Vibration             float64  `json:"vibration"`
	Temperature           float64  `json:"temperature"`
	CurrentPercentOfRated float64  `json:"currentPercentOfRated"`
	Rpm                   float64  `json:"rpm"`
	Status                string   `json:"status"`
	Context               string   `json:"context"`
	LastUpdated           string   `json:"lastUpdated"`
	OverloadTripCount     int      `json:"overloadTripCount"`
	Recommendations       []string `json:"recommendations"`
}

type UserCommand struct {
	Action string `json:"action"`
}

// --- CORE HELPERS ---

func failOnError(err error, msg string) {
	if err != nil {
		log.Fatalf("%s: %s", msg, err)
	}
}

// sendTelemetry handles the RabbitMQ publishing and timestamping
func sendTelemetry(ch *amqp.Channel, qName string, m Machine) {
	m.LastUpdated = time.Now().UTC().Format(time.RFC3339)
	body, _ := json.Marshal(m)
	ch.PublishWithContext(context.Background(), "", qName, false, false, amqp.Publishing{
		ContentType: "application/json",
		Body:        body,
	})
}

// --- SCENARIO 1: The "Standard" Machine (Random noise) ---
func simulateNormalOperation(ch *amqp.Channel, qName string, id string, name string) {
	for {
		m := Machine{
			ID: id, Name: name,
			Temperature:           70.0 + rand.Float64()*5,
			Vibration:             4.0 + rand.Float64(),
			CurrentPercentOfRated: 90.0 + rand.Float64(),
			Status:                "NORMAL", Context: "NORMAL",
		}
		sendTelemetry(ch, qName, m)
		time.Sleep(3 * time.Second)
	}
}

// --- SCENARIO 2: Heating Chain (CEP Sustained -> Forward Critical) ---
// Phase 1: Normal (75C)
// Phase 2: CEP Zone (82C) - Triggers "Temperature > 80 for 2 mins"
// Phase 3: Forward Zone (91C) - Triggers "Temperature > 90"
func simulateHeatingChain(ch *amqp.Channel, qName string, id string, name string) {
	log.Printf("[%s] SCENARIO START: Chained Heating Failure", name)
	for i := 0; ; i++ {
		temp := 75.0
		if i >= 5 && i < 15 {
			temp = 82.0 // CEP should fire
			if i == 5 {
				log.Printf("[%s] >>> Entered CEP Warning Zone (82.0C)", name)
			}
		} else if i >= 15 {
			temp = 91.0 // Forward should fire
			if i == 15 {
				log.Printf("[%s] >>> Entered FORWARD Critical Zone (91.0C)", name)
			}
		}

		m := Machine{ID: id, Name: name, Temperature: temp, Vibration: 5.0, Status: "NORMAL", Context: "NORMAL", CurrentPercentOfRated: 95.0}
		sendTelemetry(ch, qName, m)
		time.Sleep(3 * time.Second)
	}
}

// --- SCENARIO 3: Vibration Jump (CEP Jump -> Forward Limit) ---
func simulateVibrationEscalation(ch *amqp.Channel, qName string, id string, name string) {
	log.Printf("[%s] SCENARIO START: Vibration Escalation", name)
	for i := 0; ; i++ {
		vib := 4.0
		if i >= 5 && i < 10 {
			vib = 6.0 // 50% jump (Triggers CEP Jump Rule)
			if i == 5 {
				log.Printf("[%s] >>> Triggering CEP Jump (6.0)", name)
			}
		} else if i >= 10 {
			vib = 8.5 // Crosses Forward threshold (7.1)
			if i == 10 {
				log.Printf("[%s] >>> Triggering FORWARD Limit (8.5)", name)
			}
		}

		m := Machine{ID: id, Name: name, Temperature: 70.0, Vibration: vib, Status: "NORMAL", Context: "NORMAL", CurrentPercentOfRated: 95.0}
		sendTelemetry(ch, qName, m)
		time.Sleep(3 * time.Second)
	}
}

// --- NEW SCENARIO: Sustained Overload (Tests Rule 4) ---
func simulateSustainedOverload(ch *amqp.Channel, qName string, id string, name string) {
	log.Printf("[%s] START: Sustained Current Overload", name)
	for {
		m := Machine{
			ID: id, Name: name,
			Temperature: 70.0, Vibration: 4.0,
			CurrentPercentOfRated: 110.0, // Over 100%
			Status:                "NORMAL", Context: "NORMAL",
		}
		sendTelemetry(ch, qName, m)
		time.Sleep(3 * time.Second)
	}
}

// --- NEW SCENARIO: Combined Anomaly (Tests Rule 5) ---
func simulateCombinedAnomaly(ch *amqp.Channel, qName string, id string, name string) {
	log.Printf("[%s] START: Combined Temp + Vib Failure", name)
	for {
		m := Machine{
			ID: id, Name: name,
			Temperature: 86.0, // High Temp
			Vibration:   7.5,  // High Vib
			Status:      "NORMAL", Context: "NORMAL",
		}
		sendTelemetry(ch, qName, m)
		time.Sleep(3 * time.Second)
	}
}

// --- NEW SCENARIO: Post-Maintenance Trap (Tests Rule 6) ---
func simulatePostMaintenanceTrap(ch *amqp.Channel, qName string, id string, name string) {
	log.Printf("[%s] START: Post-Maintenance Heat Rise", name)
	for {
		m := Machine{
			ID: id, Name: name,
			Temperature: 82.0, // High for PM, but "Normal" for standard
			Vibration:   4.0,
			Status:      "NORMAL", Context: "POST_MAINTENANCE",
		}
		sendTelemetry(ch, qName, m)
		time.Sleep(3 * time.Second)
	}
}

// --- NEW SCENARIO: Idle Overheat (Tests Rule 8) ---
func simulateIdleOverheat(ch *amqp.Channel, qName string, id string, name string) {
	log.Printf("[%s] START: Overheating while IDLE", name)
	for {
		m := Machine{
			ID: id, Name: name,
			Temperature: 65.0, // High for IDLE
			Vibration:   1.0,
			Status:      "NORMAL", Context: "IDLE",
		}
		sendTelemetry(ch, qName, m)
		time.Sleep(3 * time.Second)
	}
}

// --- MAIN ENTRY POINT ---

func main() {
	conn, err := amqp.Dial("amqp://guest:guest@localhost:5672/")
	failOnError(err, "Failed to connect to RabbitMQ")
	defer conn.Close()

	ch, err := conn.Channel()
	failOnError(err, "Failed to open a channel")
	defer ch.Close()

	qTelemetry, _ := ch.QueueDeclare("machine_telemetry", true, false, false, false, nil)

	// --- LAUNCH SCENARIOS ---

	// Machine 1: Test the Heating Logic Chain
	//go simulateNormalOperation(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")
	//go simulateHeatingChain(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")
	go simulateNormalOperation(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")

	// Machine 2: Test the Vibration Logic Chain (Change ID to match your DB)
	// go simulateVibrationEscalation(ch, qTelemetry.Name, "M2-UUID", "Compressor B")

	log.Printf(" [*] Simulator Engine running scenarios. Press CTRL+C to stop.")
	select {}
}
