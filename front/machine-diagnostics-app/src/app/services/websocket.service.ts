import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

// 1. Change these imports to use the 'default' or 'any' fallback
import * as _SockJS from 'sockjs-client';
import * as _Stomp from 'stompjs';
import { BehaviorSubject, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private stompClient: any;

  // A stream of all incoming diagnoses
  private diagnosisSource = new BehaviorSubject<any[]>([]);
  public diagnoses$ = this.diagnosisSource.asObservable();

  private machineSource = new Subject<any>();
  public machineUpdates$ = this.machineSource.asObservable();

  constructor(private dialog: MatDialog) { }

  connect() {
    // 2. This is the magic line that fixes the "not a constructor" error
    const SockJS = (_SockJS as any).default || _SockJS;
    const Stomp = (_Stomp as any).default || _Stomp;

    const socket = new SockJS('http://localhost:8080/ws-sbnz');
    this.stompClient = Stomp.over(socket);

    const _this = this;
    this.stompClient.connect({}, (frame: any) => {
      console.log('Connected to WebSocket: ' + frame);

      _this.stompClient.subscribe('/topic/diagnosis', (message: any) => {
        if (message.body) {
          const result = JSON.parse(message.body);
          console.log("ALERT RECEIVED:", result);
          _this.handleIncomingDiagnosis(result);
        }
      });

      _this.stompClient.subscribe('/topic/machines', (message: any) => {
          this.machineSource.next(JSON.parse(message.body));
          // This updates your machine list live
          console.log("Telemetry updated");
      });
    }, (error: any) => {
        console.error("STOMP error", error);
    });
  }

   private handleIncomingDiagnosis(data: any) {
    // Add timestamp to the data
    const newEntry = {
      ...data,
      timestamp: new Date()
    };

    // Add new diagnosis to the top of the list
    const currentList = this.diagnosisSource.value;
    this.diagnosisSource.next([newEntry, ...currentList]);

    // Optional: Still show a non-intrusive toast/snack bar
    console.log("KBS Intelligence Feed Updated");
  }
}