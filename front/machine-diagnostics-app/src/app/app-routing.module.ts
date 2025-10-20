import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MachinesPageComponent } from './pages/machines-page/machines-page.component';
import { ForwardPageComponent } from './pages/forward-page/forward-page.component';
import { BackwardPageComponent } from './pages/backward-page/backward-page.component';
import { TemplatePageComponent } from './pages/template-page/template-page.component';

const routes: Routes = [
  { path: '', redirectTo: '/machines', pathMatch: 'full' },
  { path: 'machines', component: MachinesPageComponent },
  
  { path: 'forward', component: ForwardPageComponent },
  { path: 'backward', component: BackwardPageComponent },
  { path: 'cep', component: MachinesPageComponent },
  { path: 'template', component: TemplatePageComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
