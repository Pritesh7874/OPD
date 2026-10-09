import { Component, Input } from '@angular/core';

const LABELS: Record<string, string> = {
  SCHEDULED: 'Scheduled',
  IN_PROGRESS: 'In progress',
  COMPLETED: 'Completed',
};

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `<span class="badge" [class]="'badge s-' + cssSuffix">{{ label }}</span>`,
})
export class StatusBadgeComponent {
  @Input({ required: true }) status = '';

  get label(): string {
    return LABELS[this.status] ?? this.status;
  }

  get cssSuffix(): string {
    return this.status.toLowerCase().replace('_', '-');
  }
}
