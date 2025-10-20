import { Component, OnInit } from '@angular/core';
import { Machine } from '../../models/Machine';
import { MachineService } from '../../services/machine.service';

@Component({
  selector: 'app-cep-page',
  templateUrl: './cep-page.component.html',
  styleUrls: ['./cep-page.component.css']
})
export class CepPageComponent implements OnInit {
  machines: Machine[] = [];
  selectedMachine?: Machine;
  logs: string[] = [];
  loading = false;

  constructor(private machineService: MachineService) {}

  ngOnInit(): void {
    this.loadMachines();
  }

  loadMachines() {
    this.machineService.getAll().subscribe({
      next: (data) => this.machines = data,
      error: (err) => console.error(err)
    });
  }

  runCep(machine: Machine) {
    this.loading = true;
    this.logs = [];
    this.machineService.runCepOnMachine(machine.id!).subscribe({
      next: (data) => {
        this.selectedMachine = data.machine;
        this.logs = data.logs || [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }
}
