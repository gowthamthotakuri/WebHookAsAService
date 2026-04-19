# Webhook As A Service (WaaS)

A robust, scalable, and event-driven webhook delivery system designed to ensure guaranteed message dispatching to external endpoints. Built with a microservices architecture, this system utilizes RabbitMQ to handle high throughput and implements a sophisticated, non-blocking retry mechanism to gracefully manage endpoint downtime.

## 📁 Repository Structure

This repository contains multiple services that work together to ingest and dispatch webhooks. 

```text
WEBHOOKASASERVICE/
├── ingestion-service/    # Entry point: Receives webhook payloads from clients and publishes to RabbitMQ.
├── dispatcherservice/    # Core engine: Consumes messages, dispatches them, and handles exponential backoff/retries.
└── version1/             # V1 API specifications, legacy components, or shared libraries.
```

## 🚀 Key Features

* **Microservices Architecture:** Clean separation of concerns between ingestion (receiving data) and dispatching (delivering data).
* **Asynchronous Processing:** Decouples webhook delivery from the main application flow to ensure high performance and low latency.
* **Advanced Exponential Backoff:** Implements a highly customized retry strategy to prevent overwhelming struggling endpoints while ensuring eventual delivery.
* **Dead Letter Exchange (DLX) Architecture:** Utilizes RabbitMQ DLXs for routing delayed messages. The primary processing queues remain unblocked during retry wait periods.

### ⏱️ Custom Retry Schedule

The `dispatcherservice` is configured with a strict DLX routing topology. Messages that fail to deliver (e.g., due to 503 or 429 errors) are automatically retried according to the following schedule:

1.  **1st Retry:** 1 second
2.  **2nd Retry:** 5 seconds
3.  **3rd Retry:** 10 seconds
4.  **4th Retry:** 1 minute
5.  **5th Retry:** 30 minutes
6.  **6th Retry:** 6 hours
7.  **Final Retry:** 1 day

Messages that fail after the final 1-day retry are safely parked in a terminal Dead Letter Queue for manual inspection or permanent logging.

## 🛠️ Tech Stack

* **Framework:** Java / Spring Boot
* **Message Broker:** RabbitMQ
* **Architecture:** Microservices / Event-Driven

## 🚦 Getting Started

### Prerequisites

* Java 17+ 
* Maven
* RabbitMQ Server (running locally or via Docker)

### Running the Services

You will need to run the services independently to bring the full system online.

1.  **Start RabbitMQ:**
    Ensure your RabbitMQ instance is running on `localhost:5672` (or update the `application.yml` files in each service).

2.  **Run the Ingestion Service:**
    Open a terminal in the `ingestion-service` directory:
    ```bash
    cd ingestion-service
    mvn spring-boot:run
    ```

3.  **Run the Dispatcher Service:**
    Open a separate terminal in the `dispatcherservice` directory:
    ```bash
    cd dispatcherservice
    mvn spring-boot:run
    ```

## 📝 Configuration

Both services automatically provision the necessary RabbitMQ infrastructure (Exchanges, Queues, and Bindings) on startup. 
* To configure API endpoints, check the `application.yml` in `ingestion-service`.
* To modify the webhook retry intervals or add new wait queues, adjust the RabbitMQ configuration classes in `dispatcherservice`.
