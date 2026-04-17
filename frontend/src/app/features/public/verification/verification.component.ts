import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Html5Qrcode } from 'html5-qrcode';
import { FeatureApiService } from '../../../core/services/feature-api.service';

@Component({
  selector: 'app-verification',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './verification.component.html',
  styleUrl: './verification.component.css'
})
export class VerificationComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly featureApiService = inject(FeatureApiService);
  private scanner: Html5Qrcode | null = null;

  protected readonly form = this.fb.nonNullable.group({
    code: ['', [Validators.required]]
  });

  protected isScanning = false;
  protected scannerMessage = '';
  protected checked = false;
  protected result: { holder: string; status: string; date: string; valid: boolean } | null = null;

  constructor() {
    this.route.queryParamMap.subscribe((params) => {
      const code = params.get('code');
      if (code) {
        this.form.patchValue({ code });
        this.verify();
      }
    });
  }

  async startScanner(): Promise<void> {
    if (this.isScanning) {
      return;
    }

    try {
      this.scannerMessage = '';
      this.scanner = new Html5Qrcode('qr-reader');
      this.isScanning = true;

      await this.scanner.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: 220 },
        async (decodedText: string) => {
          this.form.patchValue({ code: decodedText });
          this.verify();
          await this.stopScanner();
        },
        () => {}
      );
    } catch {
      this.scannerMessage = 'Impossible de demarrer la camera. Autorise l acces camera ou utilise un upload image.';
      this.isScanning = false;
    }
  }

  async stopScanner(): Promise<void> {
    if (!this.scanner) {
      this.isScanning = false;
      return;
    }

    try {
      if (this.isScanning) {
        await this.scanner.stop();
      }
      await this.scanner.clear();
    } finally {
      this.scanner = null;
      this.isScanning = false;
    }
  }

  async onFileUpload(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    const fileScanner = new Html5Qrcode('qr-file-reader');
    try {
      const decoded = await fileScanner.scanFile(file, true);
      this.form.patchValue({ code: decoded });
      this.verify();
      this.scannerMessage = '';
    } catch {
      this.scannerMessage = 'QR non detecte dans cette image. Essaie une image plus nette.';
    } finally {
      await fileScanner.clear();
      input.value = '';
    }
  }

  verify(): void {
    this.checked = true;
    if (this.form.invalid) {
      this.result = null;
      return;
    }

    const rawInput = this.form.getRawValue().code.trim();
    const code = this.extractCode(rawInput);

    if (!code) {
      this.result = null;
      this.scannerMessage = 'Code invalide. Fournissez un code CERT-... ou une URL contenant ?code=';
      return;
    }

    this.featureApiService.verifyCertificate(code).subscribe({
      next: (response) => {
        this.result = {
          holder: response.holder || 'Inconnu',
          status: response.status,
          date: response.issueDate ?? '-',
          valid: response.valid
        };
      },
      error: () => {
        this.result = null;
        this.scannerMessage = 'Verification impossible pour le moment. Verifiez que le backend tourne sur le port 8080.';
      }
    });
  }

  private extractCode(input: string): string {
    if (!input) {
      return '';
    }

    if (input.includes('http://') || input.includes('https://')) {
      try {
        const url = new URL(input);
        return url.searchParams.get('code') ?? '';
      } catch {
        return '';
      }
    }

    return input;
  }
}
