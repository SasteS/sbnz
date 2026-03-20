import { Component, OnInit } from '@angular/core';
import { WebsocketService } from './services/websocket.service';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  
  constructor(private websocketService: WebsocketService) {}

  ngOnInit() {
    // Start listening for RabbitMQ -> Drools -> WebSocket alerts
    this.websocketService.connect();
  }
}