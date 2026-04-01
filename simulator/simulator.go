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

const (
	RoomTemp     = 25.0
	OptimalTemp  = 70.0
	BaselineVib  = 4.5
	BaselineLoad = 90.0
)

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
	currentTemp, currentVib, currentLoad := 72.0, BaselineVib, BaselineLoad
	isHalted := false

	cmdQueueName := "commands." + id
	qCmd, _ := ch.QueueDeclare(cmdQueueName, false, false, false, false, nil)
	msgs, _ := ch.Consume(qCmd.Name, "", true, false, false, false, nil)

	go func() {
		for d := range msgs {
			var action string
			json.Unmarshal(d.Body, &action)
			if action == "STOP" {
				isHalted = true
			}
			if action == "RESET" {
				isHalted = false
				currentTemp = 72.0
			}
		}
	}()

	for {
		if isHalted {
			currentVib, currentLoad = 0, 0
			if currentTemp > RoomTemp {
				currentTemp -= 1.0
			}
		} else {
			currentTemp = 70.0 + rand.Float64()*5
			currentVib = 4.0 + rand.Float64()
			currentLoad = BaselineLoad + rand.Float64()
		}
		sendTelemetry(ch, qName, Machine{ID: id, Name: name, Temperature: currentTemp, Vibration: currentVib, CurrentPercentOfRated: currentLoad, Status: "NORMAL", Context: "NORMAL"})
		time.Sleep(3 * time.Second)
	}
}

// Heating Chain (CEP Sustained -> Forward Critical) ---
func simulateHeatingChain(ch *amqp.Channel, qName string, id string, name string) {
	log.Printf("[%s] SCENARIO START: Chained Heating Failure", name)

	// Physics Constants
	const RoomTemp = 25.0
	const OptimalTemp = 70.0
	const BaselineVib = 4.5
	const BaselineLoad = 90.0

	// Internal State
	currentTemp := 75.0
	currentVib := BaselineVib
	currentLoad := BaselineLoad
	isCooling := false
	isHalted := false
	startTime := time.Now()

	cmdQueueName := "commands." + id
	qCmd, _ := ch.QueueDeclare(cmdQueueName, false, false, false, false, nil)
	msgs, _ := ch.Consume(qCmd.Name, "", true, false, false, false, nil)

	go func() {
		for d := range msgs {
			var action string
			// Strips the quotes from Java/RabbitMQ JSON strings
			if err := json.Unmarshal(d.Body, &action); err != nil {
				action = string(d.Body)
			}

			log.Printf("[%s] COMMAND PROCESSED: %s", name, action)

			switch action {
			case "ACTIVATE_COOLING":
				isCooling = true
				isHalted = false // If we cool it, we assume we want it running
			case "STOP":
				isHalted = true
				isCooling = false
			case "RESET":
				currentTemp = 75.0
				currentVib = BaselineVib
				currentLoad = BaselineLoad
				isCooling = false
				isHalted = false
				startTime = time.Now()
				log.Printf("[%s] UNIT REPAIRED. Physics and Timeline reset.", name)
			}
		}
	}()

	for {
		elapsed := time.Since(startTime).Seconds()

		if isHalted {
			// 1. HALT LOGIC: Everything goes to zero/room temp
			currentLoad = 0.0
			currentVib = 0.0
			// Temperature decays slowly toward room temperature
			if currentTemp > RoomTemp {
				currentTemp -= 2.0
				if currentTemp < RoomTemp {
					currentTemp = RoomTemp
				}
			}
		} else if isCooling {
			// 2. COOLING LOGIC: Targeted reduction
			if currentTemp > OptimalTemp {
				currentTemp -= 3.0 // Active cooling is powerful
				log.Printf("[%s] Cooling in progress... Current: %.1f°C", name, currentTemp)
			} else {
				isCooling = false // Automatically stop at optimal temp
				log.Printf("[%s] Optimal temp reached. Cooling deactivated.", name)
			}
			// Load and Vib stay normal while cooling
			currentLoad = BaselineLoad
			currentVib = BaselineVib
		} else {
			// 3. NORMAL / FAILURE TIMELINE
			if elapsed < 15 {
				currentTemp = 75.0 + rand.Float64()
				currentVib = BaselineVib + (rand.Float64() * 0.2)
				currentLoad = BaselineLoad
			} else if elapsed < 45 {
				currentTemp = 82.0 // CEP Sustained warning zone
				currentVib = BaselineVib + 0.5
				currentLoad = BaselineLoad + 5.0
			} else {
				currentTemp = 91.0 // Forward Critical zone
				currentVib = BaselineVib + 1.0
				currentLoad = BaselineLoad + 10.0
			}
		}

		m := Machine{
			ID:                    id,
			Name:                  name,
			Temperature:           currentTemp,
			Vibration:             currentVib,
			CurrentPercentOfRated: currentLoad,
			Status:                "NORMAL",
			Context:               "NORMAL",
		}
		sendTelemetry(ch, qName, m)
		time.Sleep(3 * time.Second)
	}
}

// Vibration Jump (CEP Jump -> Forward Limit) ---
func simulateVibrationEscalation(ch *amqp.Channel, qName string, id string, name string) {
	currentTemp, currentVib, currentLoad := 70.0, 4.0, BaselineLoad
	isCooling, isHalted := false, false
	startTime := time.Now()

	cmdQueueName := "commands." + id
	qCmd, _ := ch.QueueDeclare(cmdQueueName, false, false, false, false, nil)
	msgs, _ := ch.Consume(qCmd.Name, "", true, false, false, false, nil)

	go func() {
		for d := range msgs {
			var action string
			json.Unmarshal(d.Body, &action)
			switch action {
			case "ACTIVATE_COOLING":
				isCooling, isHalted = true, false
			case "STOP":
				isHalted, isCooling = true, false
			case "RESET":
				currentTemp, currentVib, currentLoad = 70.0, 4.0, BaselineLoad
				isCooling, isHalted = false, false
				startTime = time.Now()
			}
		}
	}()

	for {
		elapsed := time.Since(startTime).Seconds()
		if isHalted {
			currentLoad, currentVib = 0, 0
			if currentTemp > RoomTemp {
				currentTemp -= 1.0
			}
		} else if isCooling {
			if currentTemp > OptimalTemp {
				currentTemp -= 2.0
			} else {
				isCooling = false
			}
		} else {
			if elapsed < 15 {
				currentVib = 4.0
			} else if elapsed < 30 {
				currentVib = 6.0 // CEP Jump
			} else {
				currentVib = 8.5 // Forward Limit
			}
		}
		sendTelemetry(ch, qName, Machine{ID: id, Name: name, Temperature: currentTemp, Vibration: currentVib, CurrentPercentOfRated: currentLoad, Status: "NORMAL", Context: "NORMAL"})
		time.Sleep(3 * time.Second)
	}
}

// Sustained Overload (Tests Rule 4) ---
func simulateSustainedOverload(ch *amqp.Channel, qName string, id string, name string) {
	currentLoad := 115.0
	isHalted := false

	cmdQueueName := "commands." + id
	qCmd, _ := ch.QueueDeclare(cmdQueueName, false, false, false, false, nil)
	msgs, _ := ch.Consume(qCmd.Name, "", true, false, false, false, nil)

	go func() {
		for d := range msgs {
			var action string
			json.Unmarshal(d.Body, &action)
			if action == "STOP" {
				isHalted = true
			}
			if action == "RESET" {
				isHalted = false
				currentLoad = 115.0
			}
		}
	}()

	for {
		loadToSend := currentLoad
		if isHalted {
			loadToSend = 0.0
		}

		sendTelemetry(ch, qName, Machine{ID: id, Name: name, Temperature: 70.0, Vibration: 4.0, CurrentPercentOfRated: loadToSend, Status: "NORMAL", Context: "NORMAL"})
		time.Sleep(3 * time.Second)
	}
}

// Combined Anomaly (Tests Rule 5) ---
func simulateCombinedAnomaly(ch *amqp.Channel, qName string, id string, name string) {
	log.Printf("[%s] START: Combined Anomaly Scenario", name)
	currentTemp, currentVib := 86.0, 7.5
	isCooling, isHalted := false, false

	cmdQueueName := "commands." + id
	qCmd, _ := ch.QueueDeclare(cmdQueueName, false, false, false, false, nil)
	msgs, _ := ch.Consume(qCmd.Name, "", true, false, false, false, nil)

	go func() {
		for d := range msgs {
			var action string
			json.Unmarshal(d.Body, &action)
			switch action {
			case "ACTIVATE_COOLING":
				isCooling, isHalted = true, false
			case "STOP":
				isHalted, isCooling = true, false
			case "RESET":
				currentTemp, currentVib = 86.0, 7.5
				isCooling, isHalted = false, false
			}
		}
	}()

	for {
		if isHalted {
			currentVib = 0
			if currentTemp > RoomTemp {
				currentTemp -= 2.0
			}
		} else if isCooling {
			if currentTemp > OptimalTemp {
				currentTemp -= 2.0
			} else {
				isCooling = false
			}
		}
		sendTelemetry(ch, qName, Machine{ID: id, Name: name, Temperature: currentTemp, Vibration: currentVib, CurrentPercentOfRated: 95.0, Status: "NORMAL", Context: "NORMAL"})
		time.Sleep(3 * time.Second)
	}
}

// Post-Maintenance Trap (Tests Rule 6) ---
func simulatePostMaintenanceTrap(ch *amqp.Channel, qName string, id string, name string) {
	currentTemp, currentVib, currentLoad := 82.0, 4.0, BaselineLoad
	isCooling, isHalted := false, false
	startTime := time.Now()

	cmdQueueName := "commands." + id
	qCmd, _ := ch.QueueDeclare(cmdQueueName, false, false, false, false, nil)
	msgs, _ := ch.Consume(qCmd.Name, "", true, false, false, false, nil)

	go func() {
		for d := range msgs {
			var action string
			json.Unmarshal(d.Body, &action)
			switch action {
			case "ACTIVATE_COOLING":
				isCooling, isHalted = true, false
			case "STOP":
				isHalted, isCooling = true, false
			case "RESET":
				currentTemp = 75.0 // Reset to a safe value
				isCooling, isHalted = false, false
				startTime = time.Now()
			}
		}
	}()

	for {
		if isHalted {
			if currentTemp > RoomTemp {
				currentTemp -= 1.0
			}
		} else if isCooling {
			if currentTemp > OptimalTemp {
				currentTemp -= 2.0
			} else {
				isCooling = false
			}
		} else {
			currentTemp = 82.0
		}
		// Notice the Context stays POST_MAINTENANCE until a RESET happens
		ctx := "POST_MAINTENANCE"
		if time.Since(startTime).Seconds() < 5 && currentTemp < 80 {
			ctx = "NORMAL"
		}

		sendTelemetry(ch, qName, Machine{ID: id, Name: name, Temperature: currentTemp, Vibration: currentVib, CurrentPercentOfRated: currentLoad, Status: "NORMAL", Context: ctx})
		time.Sleep(3 * time.Second)
	}
}

// Idle Overheat (Tests Rule 8) ---
func simulateIdleOverheat(ch *amqp.Channel, qName string, id string, name string) {
	log.Printf("[%s] START: Idle Overheat Scenario", name)
	currentTemp, currentVib, currentLoad := 65.0, 1.0, 0.0
	isCooling, isHalted := false, false

	cmdQueueName := "commands." + id
	qCmd, _ := ch.QueueDeclare(cmdQueueName, false, false, false, false, nil)
	msgs, _ := ch.Consume(qCmd.Name, "", true, false, false, false, nil)

	go func() {
		for d := range msgs {
			var action string
			json.Unmarshal(d.Body, &action)
			switch action {
			case "ACTIVATE_COOLING":
				isCooling, isHalted = true, false
			case "STOP":
				isHalted, isCooling = true, false
			case "RESET":
				currentTemp, isCooling, isHalted = 65.0, false, false
			}
		}
	}()

	for {
		if isHalted {
			if currentTemp > RoomTemp {
				currentTemp -= 1.0
			}
		} else if isCooling {
			if currentTemp > 40.0 {
				currentTemp -= 2.0
			} else {
				isCooling = false
			}
		} else {
			currentTemp = 65.0 // Constant overheat for IDLE
		}
		sendTelemetry(ch, qName, Machine{ID: id, Name: name, Temperature: currentTemp, Vibration: currentVib, CurrentPercentOfRated: currentLoad, Status: "NORMAL", Context: "IDLE"})
		time.Sleep(3 * time.Second)
	}
}

func simulateMasterPlaylist(ch *amqp.Channel, qName string, id string, name string) {
	currentStage := 0
	temp, vib, load := 72.0, 4.5, 90.0
	ctx := "NORMAL"
	isHalted := false

	// NEW: State control variables
	isTransitioning := false
	transitionCounter := 0

	cmdQueueName := "commands." + id
	qCmd, _ := ch.QueueDeclare(cmdQueueName, false, false, false, false, nil)
	msgs, _ := ch.Consume(qCmd.Name, "", true, false, false, false, nil)

	go func() {
		for d := range msgs {
			var action string
			json.Unmarshal(d.Body, &action)

			if action == "STOP" {
				isHalted = true
			}
			if action == "RESET" {
				isHalted = false
				// Trigger the "Clean Buffer" phase
				isTransitioning = true
				transitionCounter = 0

				currentStage++
				if currentStage > 5 {
					currentStage = 0
				}
				log.Printf(">>> [RESET] Unit %s repaired. Flushing pipe for 15 seconds...", name)
			}
		}
	}()

	for {
		if isHalted {
			temp, vib, load = 25.0, 0.0, 0.0
		} else if isTransitioning {
			// FORCE NORMAL STATE for 5 cycles (15 seconds)
			// This gives Java and Drools time to complete the 'resetMachineHistory'
			temp, vib, load, ctx = 72.0, 4.5, 90.0, "NORMAL"
			transitionCounter++

			if transitionCounter >= 5 {
				isTransitioning = false
				log.Printf(">>> [STAGE %d] Transition complete. Scenario starting...", currentStage)
			}
		} else {
			// SCENARIO SELECTION
			switch currentStage {
			case 0:
				temp, vib, load, ctx = 72.0, 4.5, 90.0, "NORMAL"
			case 1:
				temp, vib, load, ctx = 82.0, 4.5, 90.0, "NORMAL" // CEP
			case 2:
				temp, vib, load, ctx = 91.0, 4.5, 90.0, "NORMAL" // Forward
			case 3:
				temp, vib, load, ctx = 72.0, 8.5, 90.0, "NORMAL" // CEP Jump
			case 4:
				temp, vib, load, ctx = 81.0, 4.5, 90.0, "POST_MAINTENANCE"
			case 5:
				temp, vib, load, ctx = 65.0, 1.0, 0.0, "IDLE"
			}
		}

		sendTelemetry(ch, qName, Machine{
			ID: id, Name: name, Temperature: temp, Vibration: vib,
			CurrentPercentOfRated: load, Status: "NORMAL", Context: ctx,
		})

		time.Sleep(3 * time.Second)
	}
}

func main() {
	conn, err := amqp.Dial("amqp://guest:guest@localhost:5672/")
	failOnError(err, "Failed to connect to RabbitMQ")
	defer conn.Close()

	ch, err := conn.Channel()
	failOnError(err, "Failed to open a channel")
	defer ch.Close()

	qTelemetry, _ := ch.QueueDeclare("machine_telemetry", true, false, false, false, nil)

	// Machine 1: Test the Heating Logic Chain
	//go simulateNormalOperation(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")
	//go simulateHeatingChain(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")
	//go simulateVibrationEscalation(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")
	//go simulateCombinedAnomaly(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")
	//go simulatePostMaintenanceTrap(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")
	//go simulateIdleOverheat(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")
	go simulateMasterPlaylist(ch, qTelemetry.Name, "db889345-07d2-4b59-9bf1-974c7119f246", "Pump A")

	// Machine 2: Test the Vibration Logic Chain (Change ID to match your DB)
	//go simulateNormalOperation(ch, qTelemetry.Name, "f3fd88c6-7fe1-476e-8198-8d6549d47f74", "Compressor A")
	//go simulateNormalOperation(ch, qTelemetry.Name, "ae3ecdc6-44b2-44ed-bc3b-f394935cdd17", "Compressor B")
	//go simulateNormalOperation(ch, qTelemetry.Name, "9bc18f85-da86-4aa2-8a73-4c0bc0e6e86a", "Compressor C")

	log.Printf(" [*] Simulator Engine running scenarios. Press CTRL+C to stop.")
	select {}
}
