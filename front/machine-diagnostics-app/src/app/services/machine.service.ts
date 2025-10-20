import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Machine } from '../models/Machine';

@Injectable({
  providedIn: 'root'
})
export class MachineService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // MACHINE CALLS
  getAll(): Observable<Machine[]> {
    return this.http.get<Machine[]>(`${this.apiUrl}/machines/all`);
  }

  create(name: string): Observable<Machine> {
    return this.http.post<Machine>(`${this.apiUrl}/machines/create?name=${name}`, {});
  }

  // FORWARD
  runForwardRules(machine: Machine): Observable<Machine> {
    return this.http.post<Machine>(`${this.apiUrl}/forward/run`, machine);
  }

  runForwardBatch(machines: Machine[]): Observable<Machine[]> {
    return this.http.post<Machine[]>('http://localhost:8080/api/forward/run-batch', machines);
  }

  // BACKWARD
  runBackwardChaining(machineId: string, hypothesis: string): Observable<any> {
    const payload = {
        machineId: machineId,
        hypothesis: hypothesis
    };
    return this.http.post(`${this.apiUrl}/backward-recursive/prove-machine-hypothesis`, payload);
  }

  // TEMPLATE
  // Generate and compile rules from the template/Excel
  generateTemplateRules(): Observable<string> {
    return this.http.get(`${this.apiUrl}/template/run`, { responseType: 'text' });
  }

  runTemplateDiagnostics(machine: Machine): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/template/diagnose-machine`, machine);
  }

  addDynamicRule(ruleName: string, drl: string): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/template/add-rule?ruleName=${encodeURIComponent(ruleName)}`,
      drl,
      { responseType: 'text' }
    );
  }

  // CEP
  runCepOnMachine(machineId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/cep/run-on-machine/${machineId}`, {});
  }

}
