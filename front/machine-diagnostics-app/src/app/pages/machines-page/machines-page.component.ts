import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service';
import { MatDialog } from '@angular/material/dialog';
import { CreateMachineDialogComponent } from '../../components/create-machine-dialog/create-machine-dialog.component';
import { Machine } from '../../models/Machine';
import { WebsocketService } from '../../services/websocket.service';
import { Chart, registerables } from 'chart.js';
Chart.register(...registerables);

@Component({
  selector: 'app-machines-page',
  templateUrl: './machines-page.component.html',
  styleUrls: ['./machines-page.component.css']
})
export class MachinesPageComponent implements OnInit {
  machines: Machine[] = [];
  displayedColumns = ['name', 'status', 'temperature', 'vibration', 'current'];

  // Store chart instances: { machineId: ChartObject }
  private charts: { [key: string]: any } = {};  
  // Store data history: { machineId: { labels: [], temps: [], vibs: [] } }
  private history: { [key: string]: any } = {};


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

            this.updateChartData(updatedMachine);
            
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

  sendCommand(machineId: string, action: string) {
      if (action === 'RESET') {
          const m = this.machines.find(x => x.id === machineId);
          if (m) {
              m.status = 'NORMAL';
              m.recommendations = []; // Instant UI cleanup
              this.machines = [...this.machines];
          }
      }
      this.service.sendCommand(machineId, action).subscribe();
  }

  // Add this helper function
  trackByMachineId(index: number, machine: Machine): string {
    return machine.id;
  }

  onPanelOpen(m: Machine) {
    // Initialize chart if it doesn't exist for this card
    setTimeout(() => {
        if (!this.charts[m.id]) {
            this.initChart(m);
        }
    }, 100);
  }

  private initChart(m: Machine) {
    const ctx = document.getElementById(`chart-${m.id}`) as HTMLCanvasElement;
    this.charts[m.id] = new Chart(ctx, {
      type: 'line',
      data: {
        labels: [],
        datasets: [
          { label: 'Temp', data: [], borderColor: '#bedad6', tension: 0.4, pointRadius: 0 },
          { label: 'Vib', data: [], borderColor: '#52af98', tension: 0.4, pointRadius: 0 }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: { display: false },
          y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#8b949e' } }
        },
        plugins: { legend: { display: false } }
      }
    });
  }
  
  private updateChartData(m: Machine) {
    const chart = this.charts[m.id];
    if (chart) {
      const now = new Date().toLocaleTimeString();
      chart.data.labels.push(now);
      chart.data.datasets[0].data.push(m.temperature);
      chart.data.datasets[1].data.push(m.vibration);

      // Keep only last 20 points
      if (chart.data.labels.length > 20) {
        chart.data.labels.shift();
        chart.data.datasets[0].data.shift();
        chart.data.datasets[1].data.shift();
      }
      chart.update('none'); // Update without animation for performance
    }
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
