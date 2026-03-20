import { Component } from '@angular/core';
import { WebsocketService } from '../../services/websocket.service';
import { MatDialog } from '@angular/material/dialog';
import { ReasoningDetailsDialogComponent } from '../reasoning-details-dialog/reasoning-details-dialog.component';

@Component({
  selector: 'app-intelligence-feed',
  templateUrl: './intelligence-feed.component.html',
  styleUrls: ['./intelligence-feed.component.css']
})
export class IntelligenceFeedComponent {
  diagnoses$ = this.websocketService.diagnoses$;

  constructor(
    private websocketService: WebsocketService,
    private dialog: MatDialog
  ) {}

  viewDetails(item: any) {
    this.dialog.open(ReasoningDetailsDialogComponent, {
      width: '800px',
      data: {
        machineName: item.results[0].machineName,
        hypothesis: item.results[0].hypothesis,
        proven: item.results[0].proven,
        status: item.results[0].status,
        recommendations: item.results[0].recommendations,
        logs: item.logs // Pass the reasoning tree logs here
      }
    });
  }
}