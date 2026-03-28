import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MachinesPageComponent } from './pages/machines-page/machines-page.component';
import { CreateMachineDialogComponent } from './components/create-machine-dialog/create-machine-dialog.component';

// Angular Material
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ForwardPageComponent } from './pages/forward-page/forward-page.component';
import { EditMachineDialogComponent } from './components/edit-machine-dialog/edit-machine-dialog.component';
import { MatSelectModule } from '@angular/material/select';
import { BackwardPageComponent } from './pages/backward-page/backward-page.component';
import { BackwardDialogComponent } from './components/backward-dialog/backward-dialog.component';
import { TemplatePageComponent } from './pages/template-page/template-page.component';
import { CepPageComponent } from './pages/cep-page/cep-page.component';
import { IntelligenceFeedComponent } from './components/intelligence-feed/intelligence-feed.component';

import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { ReasoningDetailsDialogComponent } from './components/reasoning-details-dialog/reasoning-details-dialog.component';

import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@NgModule({
  declarations: [
    AppComponent,
    MachinesPageComponent,
    CreateMachineDialogComponent,
    ForwardPageComponent,
    EditMachineDialogComponent,
    BackwardPageComponent,
    BackwardDialogComponent,
    TemplatePageComponent,
    CepPageComponent,
    IntelligenceFeedComponent,
    ReasoningDetailsDialogComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule,
    MatButtonModule,
    MatDialogModule,
    MatInputModule,
    MatTableModule,
    MatFormFieldModule,
    MatSelectModule,
    MatMenuModule,
    MatIconModule,
    MatBadgeModule,
    MatDividerModule,
    MatCardModule,
    MatExpansionModule,
    MatProgressBarModule
  ],
  providers: [
    provideAnimationsAsync()
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
