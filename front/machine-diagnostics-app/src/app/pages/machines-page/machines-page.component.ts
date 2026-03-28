import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service';
import { MatDialog } from '@angular/material/dialog';
import { CreateMachineDialogComponent } from '../../components/create-machine-dialog/create-machine-dialog.component';
import { Machine } from '../../models/Machine';
import { WebsocketService } from '../../services/websocket.service';

@Component({
  selector: 'app-machines-page',
  templateUrl: './machines-page.component.html',
  styleUrls: ['./machines-page.component.css']
})
export class MachinesPageComponent implements OnInit {
  machines: Machine[] = [];
  displayedColumns = ['name', 'status', 'temperature', 'vibration', 'current'];

  constructor(private service: MachineService, private dialog: MatDialog, private websocketService: WebsocketService) {}
  
  ngOnInit(): void {
    this.loadMachines();

    
     // Listen for real-time heartbeat updates
    this.websocketService.machineUpdates$.subscribe((updatedMachine: Machine) => {
        const index = this.machines.findIndex(m => m.id === updatedMachine.id);
        if (index !== -1) {
            // 1. Update the data
            this.machines[index] = updatedMachine;
            
            // 2. TRIGGER REAL-TIME UPDATE
            // This replaces the array with a new copy, forcing Angular to re-render the row
            this.machines = [...this.machines]; 
            
            console.log("UI Updated for: " + updatedMachine.name);
        }
    });
  }

  getStatusIcon(status: string): string {
    switch(status.toUpperCase()) {
      case 'NORMAL': return 'check_circle';
      case 'SUSPICIOUS': return 'help';
      case 'RISKY': return 'warning';
      case 'CRITICAL': return 'report_problem';
      default: return 'sensors';
    }
  }

  sendCommand(machineId: string, action: string): void {
    // // 1. You will need to add this method to your machine.service.ts
    // // It should call a POST endpoint like /api/machines/{id}/command
    // this.service.sendCommand(machineId, action).subscribe({
    //   next: () => console.log(`Command ${action} sent to ${machineId}`),
    //   error: (err) => console.error('Failed to send command', err)
    // });
  }

  // Add this helper function
  trackByMachineId(index: number, machine: Machine): string {
    return machine.id;
  }

  onPanelOpen(machineId: string) {
    console.log("Opening control room for: " + machineId);
    // This is where you would initialize Chart.js for this specific card
  }

  loadMachines(): void {
    this.service.getAll().subscribe((data) => (this.machines = data));
  }

  openDialog(): void {
    const dialogRef = this.dialog.open(CreateMachineDialogComponent);

    dialogRef.afterClosed().subscribe((name: string) => {
      if (name) {
        this.service.create(name).subscribe(() => this.loadMachines());
      }
    });
  }
}
