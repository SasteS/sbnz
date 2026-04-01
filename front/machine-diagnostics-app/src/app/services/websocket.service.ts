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

  public clearAlertsForMachine(machineId: string) {
      const currentList = this.diagnosisSource.value;
      // Filter out any alerts belonging to this machine
      const filteredList = currentList.filter(item => 
          item.machine?.id !== machineId && 
          item.results[0]?.machineId !== machineId
      );
      this.diagnosisSource.next(filteredList);
      console.log(`>>> UI Cleaned: Alerts removed for ${machineId}`);
  }

  private handleIncomingDiagnosis(data: any) {
      // Check for the special reset signal from Java
      if (data.type === 'SYSTEM_RESET') {
          const machineId = data.machineId;
          // Wipe the Intelligence Feed for this machine
          const currentList = this.diagnosisSource.value;
          const filtered = currentList.filter(item => item.results[0].machineId !== machineId);
          this.diagnosisSource.next(filtered);
          console.log(">>> UI State Hard Reset for machine: " + machineId);
          return; // Don't process this as a normal alert
      }

      // Normal alert logic...
      const newEntry = { ...data, timestamp: new Date() };
      const currentList = this.diagnosisSource.value;
      this.diagnosisSource.next([newEntry, ...currentList]);
  }
}