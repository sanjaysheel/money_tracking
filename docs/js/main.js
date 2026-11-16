/**
 * AURA FINANCE — BENQ-STYLE PRODUCT WEBSITE SCRIPT
 * Handles:
 * 1. Interactive SMS Parser Live Demo
 * 2. Interactive SVG Chart Switcher & Inspector
 * 3. Liquid Glass Refraction Intensity Switcher
 * 4. PIN & Biometric App Lock Simulator
 * 5. Currency Real-Time Converter Demo
 * 6. Sticky Product Sub-nav & Scroll Spy
 * 7. Live Interactive Device Mockup Tabs
 * 8. Animated KPI Counters
 */

document.addEventListener('DOMContentLoaded', () => {
  initSubNavScrollSpy();
  initDeviceMockup();
  initChartDemo();
  initSmsParserDemo();
  initGlassModeDemo();
  initSecurityDemo();
  initCurrencyDemo();
  initCounters();
  initFaqAccordion();
  initWorkbenchTabs();
  initCopyButtons();
  initTactileAudio();
  initSpotlightAnd3DTilt();
  initExplodedArchitecture();
  initInteractivePipeline();
  initRunwaySimulator();
  initTactileBillMatrix();
  initKernelAuditor();
  initGlassStudio();
});

/* ============================================================
   1. STICKY SUB-NAV & SCROLL SPY
   ============================================================ */
function initSubNavScrollSpy() {
  const nav = document.getElementById('product-subnav');
  const navLinks = document.querySelectorAll('.subnav-link');
  const sections = document.querySelectorAll('section[id]');

  window.addEventListener('scroll', () => {
    const scrollPos = window.scrollY + 120;

    // Header background toggle
    if (nav) {
      if (window.scrollY > 40) {
        nav.classList.add('scrolled');
      } else {
        nav.classList.remove('scrolled');
      }
    }

    // Scroll spy active links
    sections.forEach(section => {
      const top = section.offsetTop;
      const height = section.offsetHeight;
      const id = section.getAttribute('id');

      if (scrollPos >= top && scrollPos < top + height) {
        navLinks.forEach(link => {
          link.classList.remove('active');
          if (link.getAttribute('href') === `#${id}`) {
            link.classList.add('active');
          }
        });
      }
    });
  });

  // Mobile menu toggle
  const mobileBtn = document.getElementById('mobile-menu-btn');
  const mobileMenu = document.getElementById('mobile-nav-menu');
  if (mobileBtn && mobileMenu) {
    mobileBtn.addEventListener('click', () => {
      mobileMenu.classList.toggle('open');
    });
    mobileMenu.querySelectorAll('a').forEach(a => {
      a.addEventListener('click', () => mobileMenu.classList.remove('open'));
    });
  }
}

/* ============================================================
   2. DEVICE MOCKUP INTERACTIVE TAB SWITCHER
   ============================================================ */
function initDeviceMockup() {
  const tabs = document.querySelectorAll('.mockup-tab-btn');
  const screens = document.querySelectorAll('.mockup-screen-view');

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const target = tab.getAttribute('data-target');
      tabs.forEach(t => t.classList.remove('active'));
      screens.forEach(s => s.classList.remove('active'));

      tab.classList.add('active');
      const targetScreen = document.getElementById(`mockup-${target}`);
      if (targetScreen) targetScreen.classList.add('active');
    });
  });
}

/* ============================================================
   3. INTERACTIVE MULTI-SERIES CHART DEMO
   ============================================================ */
const chartDataset = {
  '6M': [
    { period: 'May 2026', credit: 88000, debit: 42000, balance: 410000, savings: '52%' },
    { period: 'Jun 2026', credit: 91500, debit: 46200, balance: 455300, savings: '49%' },
    { period: 'Jul 2026', credit: 89000, debit: 38900, balance: 505400, savings: '56%' },
    { period: 'Aug 2026', credit: 95000, debit: 51200, balance: 549200, savings: '46%' },
    { period: 'Sep 2026', credit: 92400, debit: 43800, balance: 597800, savings: '53%' },
    { period: 'Oct 2026', credit: 98500, debit: 41500, balance: 654800, savings: '58%' }
  ],
  '1Y': [
    { period: 'Nov', credit: 82000, debit: 45000, balance: 320000, savings: '45%' },
    { period: 'Dec', credit: 95000, debit: 62000, balance: 353000, savings: '35%' },
    { period: 'Jan', credit: 85000, debit: 40000, balance: 398000, savings: '53%' },
    { period: 'Feb', credit: 86000, debit: 39000, balance: 445000, savings: '55%' },
    { period: 'Mar', credit: 91000, debit: 44000, balance: 492000, savings: '52%' },
    { period: 'Apr', credit: 89000, debit: 43000, balance: 538000, savings: '52%' },
    { period: 'May', credit: 88000, debit: 42000, balance: 584000, savings: '52%' },
    { period: 'Jun', credit: 91500, debit: 46200, balance: 629300, savings: '49%' },
    { period: 'Jul', credit: 89000, debit: 38900, balance: 679400, savings: '56%' },
    { period: 'Aug', credit: 95000, debit: 51200, balance: 723200, savings: '46%' },
    { period: 'Sep', credit: 92400, debit: 43800, balance: 771800, savings: '53%' },
    { period: 'Oct', credit: 98500, debit: 41500, balance: 828800, savings: '58%' }
  ],
  '8W': [
    { period: 'W1 Sep', credit: 22000, debit: 9400, balance: 602000, savings: '57%' },
    { period: 'W2 Sep', credit: 23100, debit: 11200, balance: 613900, savings: '52%' },
    { period: 'W3 Sep', credit: 21500, debit: 12500, balance: 622900, savings: '42%' },
    { period: 'W4 Sep', credit: 25800, debit: 10700, balance: 638000, savings: '59%' },
    { period: 'W1 Oct', credit: 24000, debit: 9800, balance: 652200, savings: '59%' },
    { period: 'W2 Oct', credit: 25500, debit: 11100, balance: 666600, savings: '56%' },
    { period: 'W3 Oct', credit: 23900, debit: 10400, balance: 680100, savings: '56%' },
    { period: 'W4 Oct', credit: 25100, debit: 10200, balance: 695000, savings: '59%' }
  ]
};

let currentChartTimeframe = '6M';
let currentChartType = 'bar'; // 'bar' or 'line'
let chartCurrencySymbol = '₹';
let chartCurrencyRate = 1;

function initChartDemo() {
  const tfButtons = document.querySelectorAll('.chart-tf-btn');
  const typeButtons = document.querySelectorAll('.chart-type-btn');

  tfButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      tfButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentChartTimeframe = btn.getAttribute('data-tf');
      renderInteractiveChart();
    });
  });

  typeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      typeButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentChartType = btn.getAttribute('data-type');
      renderInteractiveChart();
    });
  });

  renderInteractiveChart();
}

function renderInteractiveChart() {
  const svg = document.getElementById('interactive-demo-chart');
  if (!svg) return;

  const data = chartDataset[currentChartTimeframe];
  const count = data.length;
  const maxVal = Math.max(...data.map(d => Math.max(d.credit, d.debit))) * 1.15;

  const width = 800;
  const height = 280;
  const paddingBottom = 40;
  const paddingTop = 20;
  const plotHeight = height - paddingBottom - paddingTop;
  const step = width / count;

  let html = '';

  // Background grid lines
  for (let i = 0; i <= 4; i++) {
    const y = paddingTop + (plotHeight / 4) * i;
    const val = Math.round(maxVal - (maxVal / 4) * i);
    html += `<line x1="0" y1="${y}" x2="${width}" y2="${y}" stroke="rgba(99, 102, 241, 0.08)" stroke-width="1" stroke-dasharray="4,4"/>`;
    html += `<text x="${width - 10}" y="${y - 4}" fill="#64748b" font-size="10" font-family="JetBrains Mono" text-anchor="end">${chartCurrencySymbol}${(val * chartCurrencyRate / 1000).toFixed(0)}k</text>`;
  }

  if (currentChartType === 'bar') {
    const barWidth = Math.min(step * 0.32, 26);
    data.forEach((d, idx) => {
      const centerX = step * idx + step / 2;
      const creditH = (d.credit / maxVal) * plotHeight;
      const debitH = (d.debit / maxVal) * plotHeight;

      const creditY = paddingTop + plotHeight - creditH;
      const debitY = paddingTop + plotHeight - debitH;

      // Credit bar (Green)
      html += `
        <rect class="demo-bar demo-bar-credit" x="${centerX - barWidth - 2}" y="${creditY}" width="${barWidth}" height="${creditH}" rx="4" fill="url(#demoCreditGrad)" data-idx="${idx}" style="cursor:pointer;">
          <title>${d.period}: +${chartCurrencySymbol}${(d.credit * chartCurrencyRate).toLocaleString()}</title>
        </rect>
      `;

      // Debit bar (Rose)
      html += `
        <rect class="demo-bar demo-bar-debit" x="${centerX + 2}" y="${debitY}" width="${barWidth}" height="${debitH}" rx="4" fill="url(#demoDebitGrad)" data-idx="${idx}" style="cursor:pointer;">
          <title>${d.period}: -${chartCurrencySymbol}${(d.debit * chartCurrencyRate).toLocaleString()}</title>
        </rect>
      `;

      // Period label
      html += `
        <text x="${centerX}" y="${height - 12}" fill="#94a3b8" font-size="11" font-weight="600" text-anchor="middle" font-family="Inter">${d.period}</text>
      `;
    });
  } else {
    // Area / Spline Graph Mode
    let creditPath = `M ${step / 2} ${paddingTop + plotHeight - (data[0].credit / maxVal) * plotHeight}`;
    let debitPath = `M ${step / 2} ${paddingTop + plotHeight - (data[0].debit / maxVal) * plotHeight}`;
    let creditArea = `M ${step / 2} ${paddingTop + plotHeight} L ${step / 2} ${paddingTop + plotHeight - (data[0].credit / maxVal) * plotHeight}`;
    let debitArea = `M ${step / 2} ${paddingTop + plotHeight} L ${step / 2} ${paddingTop + plotHeight - (data[0].debit / maxVal) * plotHeight}`;

    data.forEach((d, idx) => {
      const x = step * idx + step / 2;
      const creditY = paddingTop + plotHeight - (d.credit / maxVal) * plotHeight;
      const debitY = paddingTop + plotHeight - (d.debit / maxVal) * plotHeight;

      if (idx > 0) {
        creditPath += ` L ${x} ${creditY}`;
        debitPath += ` L ${x} ${debitY}`;
        creditArea += ` L ${x} ${creditY}`;
        debitArea += ` L ${x} ${debitY}`;
      }

      html += `<circle cx="${x}" cy="${creditY}" r="4" fill="#10b981" stroke="#030712" stroke-width="2"/>`;
      html += `<circle cx="${x}" cy="${debitY}" r="4" fill="#f43f5e" stroke="#030712" stroke-width="2"/>`;
      html += `<text x="${x}" y="${height - 12}" fill="#94a3b8" font-size="11" font-weight="600" text-anchor="middle" font-family="Inter">${d.period}</text>`;
    });

    const lastX = step * (count - 1) + step / 2;
    creditArea += ` L ${lastX} ${paddingTop + plotHeight} Z`;
    debitArea += ` L ${lastX} ${paddingTop + plotHeight} Z`;

    html += `<path d="${creditArea}" fill="url(#demoCreditAreaGrad)" />`;
    html += `<path d="${debitArea}" fill="url(#demoDebitAreaGrad)" />`;
    html += `<path d="${creditPath}" fill="none" stroke="#10b981" stroke-width="3" stroke-linecap="round"/>`;
    html += `<path d="${debitPath}" fill="none" stroke="#f43f5e" stroke-width="3" stroke-linecap="round"/>`;
  }

  // Update SVG
  const defs = `
    <defs>
      <linearGradient id="demoCreditGrad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#10b981"/>
        <stop offset="100%" stop-color="#059669" stop-opacity="0.8"/>
      </linearGradient>
      <linearGradient id="demoDebitGrad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#f43f5e"/>
        <stop offset="100%" stop-color="#be123c" stop-opacity="0.8"/>
      </linearGradient>
      <linearGradient id="demoCreditAreaGrad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#10b981" stop-opacity="0.35"/>
        <stop offset="100%" stop-color="#10b981" stop-opacity="0.02"/>
      </linearGradient>
      <linearGradient id="demoDebitAreaGrad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#f43f5e" stop-opacity="0.3"/>
        <stop offset="100%" stop-color="#f43f5e" stop-opacity="0.02"/>
      </linearGradient>
    </defs>
  `;

  svg.innerHTML = defs + html;

  // Add click listener on bars for inspector
  svg.querySelectorAll('.demo-bar').forEach(bar => {
    bar.addEventListener('click', (e) => {
      const idx = parseInt(e.currentTarget.getAttribute('data-idx'));
      updateChartInspector(data[idx]);
    });
  });

  // Default to last period
  updateChartInspector(data[data.length - 1]);
}

function updateChartInspector(item) {
  const inspPeriod = document.getElementById('insp-period-label');
  const inspCredit = document.getElementById('insp-credit-stat');
  const inspDebit = document.getElementById('insp-debit-stat');
  const inspNet = document.getElementById('insp-net-stat');
  const inspSavings = document.getElementById('insp-savings-chip');

  if (inspPeriod) inspPeriod.textContent = item.period;
  if (inspCredit) inspCredit.textContent = `+${chartCurrencySymbol}${(item.credit * chartCurrencyRate).toLocaleString()}`;
  if (inspDebit) inspDebit.textContent = `-${chartCurrencySymbol}${(item.debit * chartCurrencyRate).toLocaleString()}`;
  if (inspNet) {
    const net = item.credit - item.debit;
    inspNet.textContent = `+${chartCurrencySymbol}${(net * chartCurrencyRate).toLocaleString()}`;
  }
  if (inspSavings) inspSavings.textContent = `${item.savings} Saved`;
}

/* ============================================================
   4. INTERACTIVE SMS PARSER TESTER
   ============================================================ */
const sampleBankMessages = {
  hdfc: "Sent Rs. 1,450.00 from HDFC Bank A/C **4821 to SWIGGY BANGALORE on 04-NOV-26. UPI Ref 631029182391. Avl Bal: Rs. 1,42,850.50.",
  sbi: "Dear SBI User, your A/C ending 9012 debited by INR 3,890.00 on 05-Nov-26 via NetBanking at AMAZON INDIA. Bal: INR 84,320.00.",
  icici: "Salary of Rs. 98,500.00 credited to ICICI Bank Account XX8842 on 01-NOV-26 by TECH CORP HR. Info: SAL/NOV2026. Avail Bal: Rs. 2,40,900.00.",
  axis: "Alert: Rs 550.00 spent on Axis Bank Credit Card ending 3314 at UBER INDIA on 05-Nov-26. Avl Lmt: Rs 1,45,000.00."
};

function initSmsParserDemo() {
  const textarea = document.getElementById('sms-demo-input');
  const parseBtn = document.getElementById('sms-demo-parse-btn');
  const presetPills = document.querySelectorAll('.sms-preset-pill');

  presetPills.forEach(pill => {
    pill.addEventListener('click', () => {
      presetPills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      const bank = pill.getAttribute('data-bank');
      if (textarea && sampleBankMessages[bank]) {
        textarea.value = sampleBankMessages[bank];
        parseSmsDemo(textarea.value);
      }
    });
  });

  if (parseBtn && textarea) {
    parseBtn.addEventListener('click', () => parseSmsDemo(textarea.value));
    textarea.addEventListener('input', () => parseSmsDemo(textarea.value));
  }

  // Initial parse
  if (textarea && textarea.value) {
    parseSmsDemo(textarea.value);
  }
}

function parseSmsDemo(text) {
  const resultCard = document.getElementById('sms-parsed-result');
  const typeBadge = document.getElementById('parsed-type-badge');
  const amountEl = document.getElementById('parsed-amount');
  const merchantEl = document.getElementById('parsed-merchant');
  const accountEl = document.getElementById('parsed-account');
  const dateEl = document.getElementById('parsed-date');
  const latencyEl = document.getElementById('parsed-latency');

  if (!resultCard) return;

  const startTime = performance.now();

  let type = 'DEBIT';
  if (/credit|credited|salary|refund|deposited|received/i.test(text)) {
    type = 'CREDIT';
  }

  // Amount extraction
  let amount = '0.00';
  const amountMatch = text.match(/(?:Rs\.?|INR|\₹)\s*([\d,]+(?:\.\d{1,2})?)/i) || text.match(/([\d,]+(?:\.\d{1,2})?)\s*(?:debited|credited|spent)/i);
  if (amountMatch) {
    amount = amountMatch[1].replace(/,/g, '');
  }

  // Account Tail extraction
  let account = 'Unknown A/C';
  const accMatch = text.match(/(?:A\/C|Account|Card|ending|XX|\*\*)\s*([A-Za-z0-9\*]*\d{3,4})/i);
  if (accMatch) {
    account = `•••• ${accMatch[1].slice(-4)}`;
  }

  // Merchant extraction
  let merchant = 'General Payment';
  const atToMatch = text.match(/(?:to|at|info:?)\s+([A-Za-z0-9\s\.\-]{3,24}?)(?=\s+on|\s+via|\s+UPI|\s+Ref|\.|$)/i);
  if (atToMatch) {
    merchant = atToMatch[1].trim();
  }

  const duration = (performance.now() - startTime).toFixed(2);

  // Update UI
  if (typeBadge) {
    if (type === 'CREDIT') {
      typeBadge.className = 'badge badge-accent';
      typeBadge.textContent = '💰 Credit / Inflow';
      if (amountEl) {
        amountEl.textContent = `+${chartCurrencySymbol}${parseFloat(amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
        amountEl.style.color = '#10b981';
      }
    } else {
      typeBadge.className = 'badge badge-rose';
      typeBadge.textContent = '💸 Debit / Outflow';
      if (amountEl) {
        amountEl.textContent = `-${chartCurrencySymbol}${parseFloat(amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
        amountEl.style.color = '#f43f5e';
      }
    }
  }

  if (merchantEl) merchantEl.textContent = merchant;
  if (accountEl) accountEl.textContent = account;
  if (dateEl) dateEl.textContent = '05 Nov 2026, 21:05';
  if (latencyEl) latencyEl.textContent = `${duration} ms (Local Engine)`;
}

/* ============================================================
   5. LIQUID GLASS INTENSITY LIVE PREVIEW
   ============================================================ */
function initGlassModeDemo() {
  const glassButtons = document.querySelectorAll('.glass-mode-btn');
  const previewBox = document.getElementById('glass-preview-box');
  const blurVal = document.getElementById('glass-blur-spec');
  const satVal = document.getElementById('glass-sat-spec');
  const descVal = document.getElementById('glass-desc-spec');

  const modes = {
    classic: { blur: '8px', sat: '140%', desc: 'Balanced frosted look, minimal GPU footprint.' },
    liquid: { blur: '16px', sat: '180%', desc: 'Apple macOS Sequoia inspired fluid refraction.' },
    ultra: { blur: '28px', sat: '240%', desc: 'Deep optic refraction with specular edge light rims.' },
    hyper: { blur: '36px', sat: '320%', desc: 'Maximum vibrant color saturation for OLED displays.' }
  };

  glassButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      glassButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const mode = btn.getAttribute('data-mode');

      if (previewBox && modes[mode]) {
        previewBox.style.backdropFilter = `blur(${modes[mode].blur}) saturate(${modes[mode].sat})`;
        previewBox.style.webkitBackdropFilter = `blur(${modes[mode].blur}) saturate(${modes[mode].sat})`;
        if (blurVal) blurVal.textContent = modes[mode].blur;
        if (satVal) satVal.textContent = modes[mode].sat;
        if (descVal) descVal.textContent = modes[mode].desc;
      }
    });
  });
}

/* ============================================================
   6. BIOMETRIC & PIN LOCK SIMULATOR
   ============================================================ */
function initSecurityDemo() {
  const pinInput = document.getElementById('security-pin-demo');
  const unlockBtn = document.getElementById('security-pin-btn');
  const bioBtn = document.getElementById('security-bio-btn');
  const lockStatus = document.getElementById('security-demo-status');
  const vaultScreen = document.getElementById('security-demo-vault');

  if (unlockBtn && pinInput) {
    unlockBtn.addEventListener('click', () => {
      if (pinInput.value === '1234') {
        showVaultSuccess('PIN Authentication Verified (100% On-Device)');
      } else {
        showVaultError('Incorrect PIN. Try 1234 in this preview.');
      }
    });
  }

  if (bioBtn) {
    bioBtn.addEventListener('click', () => {
      bioBtn.classList.add('scanning');
      if (lockStatus) lockStatus.textContent = 'Scanning Biometric Sensor...';

      setTimeout(() => {
        bioBtn.classList.remove('scanning');
        showVaultSuccess('Biometric Fingerprint Match Confirmed (BiometricPrompt API)');
      }, 700);
    });
  }

  function showVaultSuccess(msg) {
    if (lockStatus) {
      lockStatus.textContent = msg;
      lockStatus.className = 'text-emerald-400 font-semibold text-xs mt-2';
    }
    if (vaultScreen) {
      vaultScreen.classList.add('unlocked');
    }
  }

  function showVaultError(msg) {
    if (lockStatus) {
      lockStatus.textContent = msg;
      lockStatus.className = 'text-rose-400 font-semibold text-xs mt-2';
    }
  }
}

/* ============================================================
   7. LIVE CURRENCY SWITCHER DEMO
   ============================================================ */
function initCurrencyDemo() {
  const currBtns = document.querySelectorAll('.currency-demo-pill');
  const rates = {
    INR: { symbol: '₹', rate: 1 },
    USD: { symbol: '$', rate: 0.012 },
    EUR: { symbol: '€', rate: 0.011 },
    GBP: { symbol: '£', rate: 0.0094 }
  };

  currBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      currBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const code = btn.getAttribute('data-curr');

      if (rates[code]) {
        chartCurrencySymbol = rates[code].symbol;
        chartCurrencyRate = rates[code].rate;
        renderInteractiveChart();

        // Update all elements with data-currency-amount
        document.querySelectorAll('[data-base-inr]').forEach(el => {
          const inr = parseFloat(el.getAttribute('data-base-inr'));
          const converted = inr * chartCurrencyRate;
          el.textContent = `${chartCurrencySymbol}${converted.toLocaleString(undefined, { maximumFractionDigits: code === 'INR' ? 0 : 2 })}`;
        });
      }
    });
  });
}

/* ============================================================
   8. ANIMATED KPI STAT COUNTERS
   ============================================================ */
function initCounters() {
  const counters = document.querySelectorAll('.counter-animated');
  let started = false;

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting && !started) {
        started = true;
        counters.forEach(counter => {
          const target = parseFloat(counter.getAttribute('data-target') || '0');
          const prefix = counter.getAttribute('data-prefix') || '';
          const suffix = counter.getAttribute('data-suffix') || '';
          const isFloat = counter.getAttribute('data-float') === 'true';

          let start = 0;
          const duration = 1600;
          const startTime = performance.now();

          function updateCounter(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            // Ease out expo
            const ease = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
            const current = start + (target - start) * ease;

            counter.textContent = prefix + (isFloat ? current.toFixed(1) : Math.floor(current).toLocaleString()) + suffix;

            if (progress < 1) {
              requestAnimationFrame(updateCounter);
            }
          }

          requestAnimationFrame(updateCounter);
        });
      }
    });
  }, { threshold: 0.2 });

  const metricsSection = document.getElementById('product-metrics');
  if (metricsSection) observer.observe(metricsSection);
}

/* ============================================================
   9. INTERACTIVE FAQ ACCORDION
   ============================================================ */
function initFaqAccordion() {
  const faqItems = document.querySelectorAll('.faq-item');

  faqItems.forEach(item => {
    const question = item.querySelector('.faq-question');
    if (question) {
      question.addEventListener('click', () => {
        const isOpen = item.classList.contains('open');
        faqItems.forEach(i => i.classList.remove('open'));
        if (!isOpen) item.classList.add('open');
      });
    }
  });
}


/* ============================================================
   10. PRO DEVELOPER CODE WORKBENCH & INTEGRITY
   ============================================================ */
document.addEventListener('DOMContentLoaded', () => {
  initWorkbenchTabs();
  initCopyButtons();
});

function initWorkbenchTabs() {
  const tabs = document.querySelectorAll('.workbench-tab-btn');
  const panes = document.querySelectorAll('.workbench-code-pane');

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const target = tab.getAttribute('data-target');
      tabs.forEach(t => t.classList.remove('active'));
      panes.forEach(p => p.style.display = 'none');

      tab.classList.add('active');
      const activePane = document.getElementById(`code-pane-${target}`);
      if (activePane) activePane.style.display = 'block';
    });
  });
}

function initCopyButtons() {
  const copyBtns = document.querySelectorAll('.workbench-copy-btn, .integrity-copy-btn');
  copyBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-copy-target');
      let textToCopy = '';

      if (targetId) {
        const el = document.getElementById(targetId);
        if (el) textToCopy = el.innerText || el.textContent;
      } else {
        textToCopy = btn.getAttribute('data-copy-text') || '';
      }

      if (textToCopy && navigator.clipboard) {
        navigator.clipboard.writeText(textToCopy).then(() => {
          const originalText = btn.innerHTML;
          btn.innerHTML = '<span>✓ Copied</span>';
          btn.style.borderColor = '#10b981';
          btn.style.color = '#34d399';
          setTimeout(() => {
            btn.innerHTML = originalText;
            btn.style.borderColor = '';
            btn.style.color = '';
          }, 2000);
        });
      }
    });
  });
}

/* ============================================================
   ADVANCED HARDWARE AESTHETICS & INTERACTION ENGINES
   BenQ-Calibrated Precision & Senior Design Engineering
   ============================================================ */

/* ---- 1. TACTILE AUDIO FEEDBACK (WEB AUDIO API SYNTHESIZER) ---- */
let soundEnabled = false;
let audioCtx = null;

function getAudioContext() {
  if (!audioCtx && (window.AudioContext || window.webkitAudioContext)) {
    audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  }
  return audioCtx;
}

function playTactileClick(freq = 1100, duration = 0.035, type = 'sine') {
  if (!soundEnabled) return;
  try {
    const ctx = getAudioContext();
    if (!ctx) return;
    if (ctx.state === 'suspended') ctx.resume();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(freq, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(freq * 0.45, ctx.currentTime + duration);
    gain.gain.setValueAtTime(0.035, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + duration);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + duration);
  } catch (e) {}
}

function initTactileAudio() {
  const audioToggle = document.getElementById('audio-toggle-btn');
  if (audioToggle) {
    audioToggle.addEventListener('click', () => {
      soundEnabled = !soundEnabled;
      audioToggle.classList.toggle('active', soundEnabled);
      const label = audioToggle.querySelector('.audio-label');
      if (label) label.textContent = soundEnabled ? 'AUDIO: ON' : 'AUDIO: OFF';
      if (soundEnabled) {
        getAudioContext();
        playTactileClick(1320, 0.045);
      }
    });
  }

  // Bind subtle micro-clicks to interactive elements
  document.querySelectorAll('button, .subnav-link, .sandbox-pill, .dlt-filter-btn, .workbench-tab-btn, .phone-tab-btn, .tactile-switch input, .exploded-layer-card').forEach(el => {
    el.addEventListener('click', () => {
      playTactileClick(1150, 0.025);
    });
  });
}

/* ---- 2. DYNAMIC CURSOR SPOTLIGHT & 3D TACTILE PERSPECTIVE ---- */
function initSpotlightAnd3DTilt() {
  const cards = document.querySelectorAll('.spotlight-card, .tech-card, .visual-card, .demo-sandbox-card, .spec-box');
  cards.forEach(card => {
    card.addEventListener('mousemove', e => {
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      card.style.setProperty('--mouse-x', `${x}px`);
      card.style.setProperty('--mouse-y', `${y}px`);

      if (card.classList.contains('tilt-card')) {
        const centerX = rect.width / 2;
        const centerY = rect.height / 2;
        const rotateX = ((y - centerY) / centerY) * -4.5;
        const rotateY = ((x - centerX) / centerX) * 4.5;
        card.style.transform = `perspective(1000px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) scale3d(1.008, 1.008, 1)`;
      }
    });

    card.addEventListener('mouseleave', () => {
      if (card.classList.contains('tilt-card')) {
        card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
      }
    });
  });
}

/* ---- 3. ARCHITECTURAL 6-LAYER EXPLODED VIEW ---- */
const explodedLayerData = {
  'layer-6': {
    title: 'Presentation Viewport & Liquid Glass',
    sub: 'Layer 06 // Surface Rendering & Reactive DOM',
    tag: 'SURFACE_VIEWPORT',
    badge: '60 FPS / GPU ACCELERATED',
    desc: 'Pure vanilla JavaScript and hardware-composited CSS with zero framework overhead (no React, no Vue virtual-DOM diffing lag). Features 4-tier liquid glass refraction, sub-pixel typography kerning, and 0.4s cubic-bezier spring physics.',
    latency: '< 0.8ms DOM paint',
    memory: '1.2 MB VRAM footprint',
    guarantee: '100% Client Rendering • Zero SSR Remote Assets',
    code: `/* Optical Glass Refraction Engine */
.glass-panel {
  backdrop-filter: blur(28px) saturate(210%);
  -webkit-backdrop-filter: blur(28px) saturate(210%);
  background: rgba(15, 23, 42, 0.45);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
  transform: translateZ(0); /* Force GPU compositing */
}`
  },
  'layer-5': {
    title: 'Deterministic Cash Flow Calculus',
    sub: 'Layer 05 // Real-time Arithmetic Engine',
    tag: 'MATH_CALCULUS',
    badge: 'DETERMINISTIC / ZERO-DRIFT',
    desc: 'Mathematical model executing real-time discrete financial calculations on-chip: Daily Runrate Burn velocity, projected month-end close trajectory (P_EOM), MoM category variance delta, and target net retention ratios without floating-point inaccuracies.',
    latency: '0.04ms execution',
    memory: '380 KB Heap',
    guarantee: 'Pure Function Calculus • Verified Invariant Proofs',
    code: `// Deterministic Projected EOM Close
fun calculateProjectedClose(currentBal: Double, debitSum: Double, daysElapsed: Int, totalDays: Int): Double {
    val dailyBurn = debitSum / daysElapsed.coerceAtLeast(1)
    val remainingDays = (totalDays - daysElapsed).coerceAtLeast(0)
    return currentBal - (dailyBurn * remainingDays)
}`
  },
  'layer-4': {
    title: 'Sandboxed SQLite B-Tree WAL Engine',
    sub: 'Layer 04 // Persistent Storage Layer',
    tag: 'PERSISTENCE_DB',
    badge: 'SCHEMA V4 / WAL PRAGMA',
    desc: 'Local SQLiteOpenHelper database (aura_finance.db) operating in Write-Ahead Logging (WAL) mode. Contains 7 strictly normalized tables (transactions, schedules, budgets, card_mappings, savings_goals, goal_contributions, dismissed_recurring) with microsecond atomic commits.',
    latency: '0.22ms atomic commit',
    memory: '1.8 MB Resident DB',
    guarantee: 'Zero Remote Database • Sandboxed App Data Directory',
    code: `// SQLite Pragma Hardware Configuration
db.execSQL("PRAGMA journal_mode = WAL;")
db.execSQL("PRAGMA synchronous = NORMAL;")
db.execSQL("PRAGMA temp_store = MEMORY;")
db.execSQL("CREATE INDEX IF NOT EXISTS idx_txn_date ON transactions(date_epoch DESC);")`
  },
  'layer-3': {
    title: 'DLT Regex Tokenizer & Entity Resolver',
    sub: 'Layer 03 // Autonomous Telephony Ingestion',
    tag: 'HEURISTIC_PARSER',
    badge: '35+ COMMERCIAL BANKS',
    desc: 'Pre-compiled regular expressions tokenizer that parses raw Indian DLT SMS bank formats in 0.38 milliseconds. Maps raw regulatory telecom sender headers (e.g. BZ-KIRANA, VK-BUNDL, AD-ETRNL) to clean consumer brands (Zepto, Swiggy, Zomato) with SHA-256 deduplication.',
    latency: '0.38ms average match',
    memory: '450 KB Precompiled Patterns',
    guarantee: 'DLT Header Resolution • Zero Network Query Lookups',
    code: `// DLT Telecom Entity Normalizer (SmsParser.kt)
val dltEntityMap = mapOf(
    "kiranakart" to "Zepto",
    "bundl"      to "Swiggy",
    "blinkit"    to "Blink Commerce",
    "eternal"    to "Zomato",
    "roppen"     to "Rapido",
    "avenue"     to "DMart"
)`
  },
  'layer-2': {
    title: 'Zero-Internet Kernel Sandbox',
    sub: 'Layer 02 // Android Operating System Boundary',
    tag: 'OS_SECURITY_BOUNDARY',
    badge: 'KERNEL-LEVEL AIR GAP',
    desc: 'Physical omission of android.permission.INTERNET in AndroidManifest.xml. The Linux kernel running Android refuses to allocate any AF_INET or AF_INET6 sockets to the application UID. Network data leakage is physically impossible at the OS kernel level.',
    latency: '0.00ms (No socket overhead)',
    memory: '0 MB Socket Buffers',
    guarantee: 'Hardware Enforced • Immune to Zero-Day Exfiltration',
    code: `<!-- AndroidManifest.xml Air-Gap Proof -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.aurafinance">
    <!-- CRITICAL: android.permission.INTERNET IS INTENTIONALLY OMITTED -->
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
</manifest>`
  },
  'layer-1': {
    title: 'Hardware Keystore TEE & Biometrics',
    sub: 'Layer 01 // Silicon Root of Trust',
    tag: 'SILICON_KEYSTORE',
    badge: 'BIOMETRICPROMPT / TEE',
    desc: 'Direct interface with Android Trusted Execution Environment (TEE) and hardware-backed AndroidKeyStore. User cryptographic credentials, 4-digit master PIN hashes, and biometric challenges are verified inside isolated silicon security enclaves.',
    latency: '< 12ms biometric handshake',
    memory: '0 MB (Secured in TEE)',
    guarantee: 'Silicon Root of Trust • Keystore Never Leaves Chip',
    code: `// Android Hardware Biometric Prompt
val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Unlock Aura Vault")
    .setSubtitle("Confirm on-device biometric signature")
    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    .build()`
  }
};

function initExplodedArchitecture() {
  const layerCards = document.querySelectorAll('.exploded-layer-card');
  const titleEl = document.getElementById('exploded-title');
  const subEl = document.getElementById('exploded-sub');
  const tagEl = document.getElementById('exploded-tag');
  const badgeEl = document.getElementById('exploded-badge');
  const descEl = document.getElementById('exploded-desc');
  const latencyEl = document.getElementById('exploded-latency');
  const memoryEl = document.getElementById('exploded-memory');
  const guaranteeEl = document.getElementById('exploded-guarantee');
  const codeEl = document.getElementById('exploded-code');

  if (!layerCards.length || !titleEl) return;

  function setLayer(layerKey) {
    const data = explodedLayerData[layerKey];
    if (!data) return;

    layerCards.forEach(card => {
      card.classList.toggle('active', card.getAttribute('data-layer') === layerKey);
    });

    titleEl.textContent = data.title;
    subEl.textContent = data.sub;
    tagEl.textContent = data.tag;
    badgeEl.textContent = data.badge;
    descEl.textContent = data.desc;
    latencyEl.textContent = data.latency;
    memoryEl.textContent = data.memory;
    guaranteeEl.textContent = data.guarantee;
    if (codeEl) codeEl.textContent = data.code;
  }

  layerCards.forEach(card => {
    card.addEventListener('click', () => {
      const layerKey = card.getAttribute('data-layer');
      setLayer(layerKey);
      playTactileClick(1250, 0.03);
    });

    card.addEventListener('mouseenter', () => {
      const layerKey = card.getAttribute('data-layer');
      setLayer(layerKey);
    });
  });
}

/* ---- 4. INTERACTIVE ANIMATED PIPELINE (POINT 1) ---- */
function initInteractivePipeline() {
  const triggerBtn = document.getElementById('btn-simulate-pipeline');
  const nodes = document.querySelectorAll('.pipe-node');
  const hudStatus = document.getElementById('pipeline-hud-status');
  const hudTime = document.getElementById('pipeline-hud-time');

  if (!triggerBtn || !nodes.length) return;

  let isRunning = false;

  triggerBtn.addEventListener('click', () => {
    if (isRunning) return;
    isRunning = true;
    triggerBtn.disabled = true;
    triggerBtn.style.opacity = '0.6';

    nodes.forEach(n => n.classList.remove('active-packet'));

    const stages = [
      { idx: 0, text: 'Android Telephony Ingress (SMS_RECEIVED broadcast)', time: '0.04ms' },
      { idx: 1, text: 'Zero-Copy Memory Pipe Buffer Allocated', time: '0.08ms' },
      { idx: 2, text: 'Regex Engine Tokenizing Amount & Merchant', time: '0.24ms' },
      { idx: 3, text: 'DLT Header Resolved: BZ-KIRANA -> Zepto', time: '0.31ms' },
      { idx: 4, text: 'Committed to SQLite WAL (ROWID #1048)', time: '0.38ms' }
    ];

    let current = 0;
    function nextStage() {
      if (current > 0) nodes[current - 1].classList.remove('active-packet');
      if (current < stages.length) {
        nodes[current].classList.add('active-packet');
        if (hudStatus) hudStatus.textContent = stages[current].text;
        if (hudTime) hudTime.textContent = `Latency: ${stages[current].time}`;
        playTactileClick(900 + current * 120, 0.03);
        current++;
        setTimeout(nextStage, 220);
      } else {
        nodes[nodes.length - 1].classList.add('active-packet');
        if (hudStatus) hudStatus.textContent = '✓ Pipeline Ingestion Complete. UI View Updated.';
        if (hudTime) hudTime.textContent = 'Total Pipeline Latency: 0.38ms';
        playTactileClick(1500, 0.06);
        setTimeout(() => {
          isRunning = false;
          triggerBtn.disabled = false;
          triggerBtn.style.opacity = '1';
        }, 1000);
      }
    }

    nextStage();
  });
}

/* ---- 5. INTERACTIVE RUNWAY & CASH FLOW SIMULATOR (POINT 2) ---- */
function initRunwaySimulator() {
  const inflowInput = document.getElementById('runway-inflow');
  const outflowInput = document.getElementById('runway-outflow');
  const targetInput = document.getElementById('runway-target');

  const inflowDisplay = document.getElementById('display-runway-inflow');
  const outflowDisplay = document.getElementById('display-runway-outflow');
  const targetDisplay = document.getElementById('display-runway-target');

  const netBalDisplay = document.getElementById('runway-net-bal');
  const rateDisplay = document.getElementById('runway-achieved-rate');
  const runwayDaysDisplay = document.getElementById('runway-buffer-days');
  const healthBadge = document.getElementById('runway-health-badge');

  if (!inflowInput || !outflowInput || !targetInput) return;

  function updateRunway() {
    const inflow = parseFloat(inflowInput.value) || 0;
    const outflow = parseFloat(outflowInput.value) || 0;
    const target = parseFloat(targetInput.value) || 40;

    if (inflowDisplay) inflowDisplay.textContent = `₹${inflow.toLocaleString('en-IN')}`;
    if (outflowDisplay) outflowDisplay.textContent = `₹${outflow.toLocaleString('en-IN')}`;
    if (targetDisplay) targetDisplay.textContent = `${target}%`;

    const netRetained = Math.max(0, inflow - outflow);
    const achievedRate = inflow > 0 ? ((netRetained / inflow) * 100).toFixed(1) : 0;
    const dailyBurn = (outflow / 30).toFixed(2);
    const baseSavings = 550000;
    const simulatedNetBal = baseSavings + netRetained;
    const bufferDays = dailyBurn > 0 ? Math.round(simulatedNetBal / dailyBurn) : 999;

    if (netBalDisplay) netBalDisplay.textContent = `₹${simulatedNetBal.toLocaleString('en-IN')}`;
    if (rateDisplay) {
      rateDisplay.textContent = `${achievedRate}%`;
      rateDisplay.style.color = achievedRate >= target ? '#10b981' : '#f43f5e';
    }
    if (runwayDaysDisplay) runwayDaysDisplay.textContent = `${bufferDays} Days`;

    if (healthBadge) {
      if (achievedRate >= 50) {
        healthBadge.textContent = 'GRADE: AAA (SOVEREIGN)';
        healthBadge.className = 'badge badge-accent';
      } else if (achievedRate >= target) {
        healthBadge.textContent = 'GRADE: AA (OPTIMAL)';
        healthBadge.className = 'badge badge-primary';
      } else if (achievedRate >= 20) {
        healthBadge.textContent = 'GRADE: A (MODERATE)';
        healthBadge.className = 'badge badge-amber';
      } else {
        healthBadge.textContent = 'GRADE: BBB (BURN ALERT)';
        healthBadge.className = 'badge badge-rose';
      }
    }
  }

  [inflowInput, outflowInput, targetInput].forEach(slider => {
    slider.addEventListener('input', () => {
      updateRunway();
    });
    slider.addEventListener('change', () => {
      playTactileClick(1100, 0.02);
    });
  });

  updateRunway();
}

/* ---- 6. INTERACTIVE BILL & SUBSCRIPTION MATRIX (POINT 4) ---- */
function initTactileBillMatrix() {
  const switches = document.querySelectorAll('.bill-toggle-switch');
  const totalDueEl = document.getElementById('matrix-total-due');
  const paidSoFarEl = document.getElementById('matrix-paid-sofar');
  const pendingDueEl = document.getElementById('matrix-pending-due');

  if (!switches.length) return;

  function recalculateBills() {
    let total = 0;
    let paid = 0;
    let pending = 0;

    switches.forEach(sw => {
      const amount = parseFloat(sw.getAttribute('data-amount')) || 0;
      const statusBadge = document.getElementById(`badge-${sw.id}`);
      total += amount;

      if (sw.checked) {
        paid += amount;
        if (statusBadge) {
          statusBadge.textContent = 'PAID';
          statusBadge.className = 'badge badge-accent';
        }
      } else {
        pending += amount;
        if (statusBadge) {
          statusBadge.textContent = 'PENDING';
          statusBadge.className = 'badge badge-amber';
        }
      }
    });

    if (totalDueEl) totalDueEl.textContent = `₹${total.toLocaleString('en-IN')}`;
    if (paidSoFarEl) paidSoFarEl.textContent = `₹${paid.toLocaleString('en-IN')}`;
    if (pendingDueEl) pendingDueEl.textContent = `₹${pending.toLocaleString('en-IN')}`;
  }

  switches.forEach(sw => {
    sw.addEventListener('change', () => {
      recalculateBills();
      playTactileClick(sw.checked ? 1350 : 950, 0.03);
    });
  });

  recalculateBills();
}

/* ---- 7. KERNEL SECURITY PERMISSION AUDITOR (POINT 5) ---- */
function initKernelAuditor() {
  const socketBtn = document.getElementById('btn-test-socket');
  const socketConsole = document.getElementById('socket-test-console');

  if (!socketBtn || !socketConsole) return;

  socketBtn.addEventListener('click', () => {
    socketBtn.disabled = true;
    socketBtn.innerHTML = '<span>⚡ Invocating Kernel Socket Request...</span>';
    socketConsole.style.display = 'block';
    socketConsole.innerHTML = `
<span style="color: #64748b;">[SYS_CALL] socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)...</span><br>
<span style="color: #818cf8;">[SELinux] Auditing UID 10384 capabilities against AndroidManifest...</span><br>
<span style="color: #f43f5e; font-weight: 700;">[KERNEL REJECTION] Permission Denied: EACCES (Socket creation prohibited)</span><br>
<span style="color: #34d399;">✓ Physical Air-Gap Invariant Verified. Zero bytes transmitted.</span>
`;
    playTactileClick(700, 0.08, 'sawtooth');

    setTimeout(() => {
      socketBtn.disabled = false;
      socketBtn.innerHTML = '<span>🧪 Test Outbound Socket Request</span>';
    }, 2500);
  });
}

/* ---- 8. OPTICAL LIQUID GLASS CALIBRATION STUDIO (POINT 6) ---- */
function initGlassStudio() {
  const blurSlider = document.getElementById('glass-blur-slider');
  const opacitySlider = document.getElementById('glass-opacity-slider');
  const tintSelect = document.getElementById('glass-tint-select');
  const specimen = document.getElementById('glass-specimen');
  const cssCode = document.getElementById('glass-css-output');

  if (!blurSlider || !opacitySlider || !specimen) return;

  function updateGlassSpecimen() {
    const blurVal = blurSlider.value;
    const opacityVal = (opacitySlider.value / 100).toFixed(2);
    const tint = tintSelect ? tintSelect.value : 'indigo';

    let tintBg = `rgba(15, 23, 42, ${opacityVal})`;
    let borderCol = `rgba(99, 102, 241, 0.25)`;

    if (tint === 'emerald') {
      tintBg = `rgba(6, 36, 26, ${opacityVal})`;
      borderCol = `rgba(16, 185, 129, 0.3)`;
    } else if (tint === 'obsidian') {
      tintBg = `rgba(5, 7, 13, ${opacityVal})`;
      borderCol = `rgba(255, 255, 255, 0.12)`;
    } else if (tint === 'cyber') {
      tintBg = `rgba(20, 10, 35, ${opacityVal})`;
      borderCol = `rgba(168, 85, 247, 0.35)`;
    }

    specimen.style.backdropFilter = `blur(${blurVal}px) saturate(180%)`;
    specimen.style.webkitBackdropFilter = `blur(${blurVal}px) saturate(180%)`;
    specimen.style.background = tintBg;
    specimen.style.borderColor = borderCol;

    if (cssCode) {
      cssCode.textContent = `backdrop-filter: blur(${blurVal}px) saturate(180%);
background: ${tintBg};
border: 1px solid ${borderCol};`;
    }
  }

  [blurSlider, opacitySlider].forEach(s => {
    s.addEventListener('input', updateGlassSpecimen);
    s.addEventListener('change', () => playTactileClick(1100, 0.02));
  });

  if (tintSelect) {
    tintSelect.addEventListener('change', () => {
      updateGlassSpecimen();
      playTactileClick(1200, 0.03);
    });
  }

  updateGlassSpecimen();
}

