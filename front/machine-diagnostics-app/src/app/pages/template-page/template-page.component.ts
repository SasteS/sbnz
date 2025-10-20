// src/app/pages/template-page/template-page.component.ts

import { Component, OnInit } from '@angular/core';

import { MachineService } from '../../services/machine.service';
import { Machine } from '../../models/Machine';

@Component({
  selector: 'app-template-page',
  templateUrl: './template-page.component.html',
  styleUrls: ['./template-page.component.css']
})
export class TemplatePageComponent implements OnInit {
  machines: Machine[] = [];
  loading = false;
  statusMessage = '';
  // Property to hold the logs from the last template execution
  templateLogs: string[] = [];

  // Dynamic rule creation fields
  newRuleName = '';
  newRuleContent = '';

  constructor(private machineService: MachineService) {}

  ngOnInit(): void {
    this.loadMachines();
  }

  loadMachines(): void {
    this.machineService.getAll().subscribe({
      next: (data) => (this.machines = data),
      error: (err) => {
        console.error('Error loading machines:', err);
        this.statusMessage = 'Error loading machines.';
      }
    });
  }

  // Action for the global button
  generateRules(): void {
    this.loading = true;
    this.templateLogs = []; // Clear logs on new generation
    this.statusMessage = 'Generating and compiling rules from template...';

    this.machineService.generateTemplateRules().subscribe({
        next: (response) => {
        this.statusMessage = response; // e.g., "Template rules executed!"
        this.loading = false;
      },
      error: (err) => {
        this.statusMessage = 'Error generating rules. Check backend logs.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  // Action for the single machine button
  runTemplateOnMachine(machine: Machine): void {
    if (this.loading) return;

    this.loading = true;
    this.templateLogs = []; // Clear previous logs
    this.statusMessage = `Running template rules on ${machine.name}...`;

    // The service is expected to return { machine: Machine, logs: string[] }
    this.machineService.runTemplateDiagnostics(machine).subscribe({
      next: (response) => {
        // Extract the updated machine and logs
        const updatedMachine = response.machine;
        this.templateLogs = response.logs || []; 

        console.log("LOGS", this.templateLogs)

        // Update the local machine list
        const index = this.machines.findIndex(m => m.id === updatedMachine.id);
        if (index !== -1) {
          Object.assign(this.machines[index], updatedMachine);
        }

        // Set status message based on logs
        const firedMessage = this.templateLogs.find(log => log.includes("rules fired"));
        this.statusMessage = firedMessage 
            ? `${machine.name} diagnosed. ${firedMessage}` 
            : `${machine.name} updated successfully by template rules.`;
      },
      error: (err) => {
        // Handle error logs if available in the error response
        this.templateLogs = err.error?.logs || []; 
        this.statusMessage = `Error diagnosing ${machine.name}. Check console for details.`;
        console.error(err);
      }
    }).add(() => {
      this.loading = false;
    });
  }

  addDynamicRule(): void {
    if (!this.newRuleName.trim() || !this.newRuleContent.trim()) {
      this.statusMessage = 'Please provide both rule name and DRL content.';
      return;
    }

    this.loading = true;
    this.statusMessage = `Adding new rule "${this.newRuleName}"...`;

    this.machineService.addDynamicRule(this.newRuleName, this.newRuleContent).subscribe({
      next: (response) => {
        this.statusMessage = `Rule "${this.newRuleName}" added successfully.`;
        this.newRuleName = '';
        this.newRuleContent = '';
      },
      error: (err) => {
        console.error(err);
        this.statusMessage = 'Failed to add rule. Check backend logs.';
      }
    }).add(() => (this.loading = false));
  }

}