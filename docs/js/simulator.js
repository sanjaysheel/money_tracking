/**
 * AURA FINANCE — 100% INTERACTIVE FULL MOBILE PHONE SIMULATOR
 * Virtual Device Sandbox Engine
 * Handles:
 * - Live State Management (Transactions, Budgets, Recurring Bills, Goals)
 * - Tab Switching (Dashboard, Insights, Scheduler, Settings)
 * - Live Adding of Transactions (Manual + Bank SMS Scanner)
 * - Dynamic Island Push Notification Simulator
 * - Biometric & 4-Digit PIN Lockscreen Gate
 * - Interactive Ledger Filtering & Live Search
 * - Interactive Recurring Bills Paid/Pending Toggle
 * - Real-Time Currency Switcher (INR, USD, EUR, GBP)
 * - Real-Time Theme (Dark/Light) & Glass Refraction Switcher
 * - Live Text Scaling Slider (80% - 120%)
 */

// Initial Seed Dataset (Truthful Replica of Real App)
const defaultSimulatorData = {
  currency: 'INR',
  currencyRate: 1,
  currencySymbol: '₹',
  theme: 'dark',
  glassMode: 'liquid',
  fontScale: 100,
  activeTab: 'dashboard',
  filterType: 'ALL',
  filterYear: 'ALL',
  filterMonth: 'ALL',
  filterAccount: 'ALL',
  searchQuery: '',
  isLocked: false,
  enteredPin: '',
  transactions: [
    { id: 1, name: 'Swiggy Food Delivery', amount: 1450, type: 'DEBIT', category: 'Food', account: '4821', date: '05 Nov 2026', icon: '🍔' },
    { id: 2, name: 'Amazon Prime Shopping', amount: 3890, type: 'DEBIT', category: 'Shopping', account: '9012', date: '04 Nov 2026', icon: '📦' },
    { id: 3, name: 'Tech Corp Monthly Salary', amount: 98500, type: 'CREDIT', category: 'Salary', account: '8842', date: '01 Nov 2026', icon: '💼' },
    { id: 4, name: 'Uber Premier Rides', amount: 550, type: 'DEBIT', category: 'Transport', account: '3314', date: '02 Nov 2026', icon: '🚗' },
    { id: 5, name: 'Netflix Premium 4K', amount: 649, type: 'DEBIT', category: 'Bills', account: '4821', date: '28 Oct 2026', icon: '🎬' },
    { id: 6, name: 'Fresh Supermarket Groceries', amount: 2240, type: 'DEBIT', category: 'Food', account: '9012', date: '25 Oct 2026', icon: '🥦' },
    { id: 7, name: 'State Electricity Board', amount: 1850, type: 'DEBIT', category: 'Bills', account: '4821', date: '22 Oct 2026', icon: '💡' }
  ],
  bills: [
    { id: 1, title: 'Apartment Rent Transfer', amount: 18000, due: '1st of every month', paid: true, icon: '🏠' },
    { id: 2, title: 'ACT Fibernet Gigabit', amount: 1179, due: 'Due in 3 days', paid: false, icon: '⚡' },
    { id: 3, title: 'Netflix 4K UHD Plan', amount: 649, due: 'Due in 7 days', paid: false, icon: '🎬' },
    { id: 4, title: 'HDFC Life Mutual Fund SIP', amount: 2500, due: 'Due in 12 days', paid: false, icon: '📈' },
    { id: 5, title: 'Gym & Fitness Membership', amount: 2000, due: 'Due in 18 days', paid: false, icon: '💪' }
  ],
  budgets: [
    { category: 'Food & Dining', spent: 14200, limit: 18000, icon: '🍔' },
    { category: 'Shopping & Electronics', spent: 8500, limit: 12000, icon: '🛍️' },
    { category: 'Utilities & Subscriptions', spent: 5670, limit: 6000, icon: '💡' },
    { category: 'Transport & Commute', spent: 2450, limit: 5000, icon: '🚗' }
  ],
  goals: [
    { title: 'Emergency Liquid Vault', current: 250000, target: 300000, icon: '🛡️' },
    { title: 'New MacBook M4 Pro', current: 120000, target: 180000, icon: '💻' },
    { title: 'Japan Vacation 2027', current: 80000, target: 200000, icon: '✈️' }
  ]
};

// Global Simulator State Clone
let simState = JSON.parse(JSON.stringify(defaultSimulatorData));

// Currency Conversion Tables
const simCurrencyRates = {
  INR: { symbol: '₹', rate: 1 },
  USD: { symbol: '$', rate: 0.012 },
  EUR: { symbol: '€', rate: 0.011 },
  GBP: { symbol: '£', rate: 0.0094 }
};

document.addEventListener('DOMContentLoaded', () => {
  initPhoneSimulator();
});

function initPhoneSimulator() {
  updateLivePhoneClock();
  setInterval(updateLivePhoneClock, 1000);

  setupBottomNav();
  setupLedgerFilters();
  setupModals();
  setupLockscreen();
  setupCompanionRemote();
  renderAllSimulatorViews();
}

/* ============================================================
   1. LIVE CLOCK IN STATUS BAR
   ============================================================ */
function updateLivePhoneClock() {
  const clockEl = document.getElementById('phone-status-clock');
  if (clockEl) {
    const now = new Date();
    const hrs = String(now.getHours()).padStart(2, '0');
    const mins = String(now.getMinutes()).padStart(2, '0');
    clockEl.textContent = `${hrs}:${mins}`;
  }
}

/* ============================================================
   2. FORMATTING HELPERS
   ============================================================ */
function formatSimMoney(amountInINR) {
  const converted = amountInINR * simState.currencyRate;
  const digits = simState.currency === 'INR' ? 2 : 2;
  return `${simState.currencySymbol}${converted.toLocaleString('en-US', { minimumFractionDigits: digits, maximumFractionDigits: digits })}`;
}

/* ============================================================
   3. BOTTOM NAVIGATION TAB SWITCHER
   ============================================================ */
function setupBottomNav() {
  const navBtns = document.querySelectorAll('.sim-nav-btn');
  navBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetTab = btn.getAttribute('data-tab');
      switchSimulatorTab(targetTab);
    });
  });
}

function switchSimulatorTab(tabName) {
  simState.activeTab = tabName;

  // Update phone nav dock buttons
  document.querySelectorAll('.sim-nav-btn').forEach(b => {
    b.classList.remove('active');
    if (b.getAttribute('data-tab') === tabName) {
      b.classList.add('active');
    }
  });

  // Synchronize companion remote buttons
  document.querySelectorAll('.remote-tab-btn').forEach(b => {
    b.classList.toggle('active', b.getAttribute('data-tab') === tabName);
  });

  // Update tab screens
  document.querySelectorAll('.sim-tab-screen').forEach(screen => {
    screen.classList.remove('active');
  });

  const activeScreen = document.getElementById(`sim-tab-${tabName}`);
  if (activeScreen) {
    activeScreen.classList.add('active');
  }

  // Scroll viewport to top
  const viewport = document.getElementById('sim-viewport');
  if (viewport) viewport.scrollTop = 0;

  // Refresh tab-specific components
  if (tabName === 'insights') {
    renderSimSvgChart();
  }
}

/* ============================================================
   4. RENDER DASHBOARD (KPIs, LEDGER, FILTERS)
   ============================================================ */
function renderDashboard() {
  let totalInflow = 0;
  let totalOutflow = 0;

  simState.transactions.forEach(t => {
    if (t.type === 'CREDIT') totalInflow += t.amount;
    else totalOutflow += t.amount;
  });

  const baseNetWorth = 597800; // base capital
  const currentNetBalance = baseNetWorth + totalInflow - totalOutflow;
  const savingsRate = totalInflow > 0 ? (((totalInflow - totalOutflow) / totalInflow) * 100).toFixed(1) : 0;

  // Update Hero Card
  const netEl = document.getElementById('sim-kpi-net-balance');
  const inflowEl = document.getElementById('sim-kpi-inflow');
  const outflowEl = document.getElementById('sim-kpi-outflow');
  const savingsEl = document.getElementById('sim-kpi-savings-rate');

  if (netEl) netEl.textContent = formatSimMoney(currentNetBalance);
  if (inflowEl) inflowEl.textContent = `+${formatSimMoney(totalInflow)}`;
  if (outflowEl) outflowEl.textContent = `-${formatSimMoney(totalOutflow)}`;
  if (savingsEl) savingsEl.textContent = `${savingsRate}% Saved`;

  // Render Ledger
  renderLedgerList();
}

function renderLedgerList() {
  const container = document.getElementById('sim-tx-list-container');
  const countBadge = document.getElementById('sim-ledger-count');
  if (!container) return;

  // Filter transactions
  const filtered = simState.transactions.filter(t => {
    // Type Filter
    if (simState.filterType === 'DEBIT' && t.type !== 'DEBIT') return false;
    if (simState.filterType === 'CREDIT' && t.type !== 'CREDIT') return false;

    // Account Tail Filter
    if (simState.filterAccount !== 'ALL' && t.account !== simState.filterAccount) return false;

    // Search Query Filter
    if (simState.searchQuery) {
      const q = simState.searchQuery.toLowerCase();
      const matchName = t.name.toLowerCase().includes(q);
      const matchCat = t.category.toLowerCase().includes(q);
      const matchTail = t.account.includes(q);
      if (!matchName && !matchCat && !matchTail) return false;
    }

    return true;
  });

  if (countBadge) countBadge.textContent = `${filtered.length} entries`;

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 24px 10px; color: var(--sim-text-muted); font-size: 11px;">
        <span style="font-size: 20px; display: block; margin-bottom: 6px;">🔍</span>
        No transactions match the selected filters.
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(t => {
    const isCredit = t.type === 'CREDIT';
    const sign = isCredit ? '+' : '-';
    const amountClass = isCredit ? 'credit' : 'debit';
    const iconBg = isCredit ? 'rgba(16, 185, 129, 0.15)' : 'rgba(244, 63, 94, 0.15)';
    const iconColor = isCredit ? '#10b981' : '#f43f5e';

    return `
      <div class="sim-tx-item" onclick="showTxDetail(${t.id})">
        <div class="sim-tx-left">
          <div class="sim-tx-icon" style="background: ${iconBg}; color: ${iconColor};">
            ${t.icon || (isCredit ? '💰' : '💸')}
          </div>
          <div>
            <div class="sim-tx-name">${t.name}</div>
            <div class="sim-tx-meta">${t.category} • Card ••${t.account} • ${t.date}</div>
          </div>
        </div>
        <div class="sim-tx-amount ${amountClass}">
          ${sign}${formatSimMoney(t.amount)}
        </div>
      </div>
    `;
  }).join('');
}

function setupLedgerFilters() {
  // Filter pills
  document.querySelectorAll('.sim-type-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.sim-type-pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      simState.filterType = pill.getAttribute('data-type');
      renderLedgerList();
    });
  });

  document.querySelectorAll('.sim-acc-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.sim-acc-pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      simState.filterAccount = pill.getAttribute('data-acc');
      renderLedgerList();
    });
  });

  // Search input
  const searchInput = document.getElementById('sim-ledger-search');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      simState.searchQuery = e.target.value.trim();
      renderLedgerList();
    });
  }
}

/* ============================================================
   5. RENDER INSIGHTS (ANALYTICS, SVG CHART, BUDGETS, GOALS)
   ============================================================ */
function renderInsights() {
  // Daily Burn calculation
  const burnEl = document.getElementById('sim-insight-daily-burn');
  const monthEndEl = document.getElementById('sim-insight-month-end');
  const prevMonthEl = document.getElementById('sim-insight-prev-month');

  let totalDebit = 0;
  simState.transactions.filter(t => t.type === 'DEBIT').forEach(t => totalDebit += t.amount);

  const daysElapsed = 5;
  const daysInMonth = 30;
  const dailyBurn = Math.round(totalDebit / daysElapsed);
  const projectedSpend = Math.round(dailyBurn * daysInMonth);

  if (burnEl) burnEl.textContent = `${formatSimMoney(dailyBurn)} / d`;
  if (monthEndEl) monthEndEl.textContent = formatSimMoney(projectedSpend);
  if (prevMonthEl) prevMonthEl.textContent = formatSimMoney(43800);

  // Render Category Budgets
  const budgetContainer = document.getElementById('sim-budgets-list');
  if (budgetContainer) {
    budgetContainer.innerHTML = simState.budgets.map(b => {
      const pct = Math.min(Math.round((b.spent / b.limit) * 100), 100);
      const isWarn = pct >= 90;
      const barColor = isWarn ? '#f59e0b' : '#6366f1';

      return `
        <div style="background: var(--sim-bg-card-subtle); border: 1px solid var(--sim-border); border-radius: 12px; padding: 10px; margin-bottom: 8px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
            <div style="font-size: 11px; font-weight: 700; color: var(--sim-text-main); display: flex; align-items: center; gap: 6px;">
              <span>${b.icon}</span>
              <span>${b.category}</span>
            </div>
            <div style="font-size: 10px; font-family: 'JetBrains Mono', monospace; font-weight: 700; color: ${isWarn ? '#f59e0b' : 'var(--sim-text-muted)'};">
              ${formatSimMoney(b.spent)} / ${formatSimMoney(b.limit)}
            </div>
          </div>
          <div style="width: 100%; height: 5px; background: rgba(255,255,255,0.08); border-radius: 3px; overflow: hidden; margin-top: 4px;">
            <div style="width: ${pct}%; height: 100%; background: ${barColor}; border-radius: 3px;"></div>
          </div>
        </div>
      `;
    }).join('');
  }

  // Render Savings Goals
  const goalsContainer = document.getElementById('sim-goals-list');
  if (goalsContainer) {
    goalsContainer.innerHTML = simState.goals.map(g => {
      const pct = Math.min(Math.round((g.current / g.target) * 100), 100);
      return `
        <div style="background: var(--sim-bg-card-subtle); border: 1px solid var(--sim-border); border-radius: 12px; padding: 10px; margin-bottom: 8px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
            <div style="font-size: 11px; font-weight: 700; color: var(--sim-text-main); display: flex; align-items: center; gap: 6px;">
              <span>${g.icon}</span>
              <span>${g.title}</span>
            </div>
            <span style="font-size: 9px; font-weight: 700; padding: 2px 6px; border-radius: 6px; background: rgba(16, 185, 129, 0.15); color: #34d399;">${pct}%</span>
          </div>
          <div style="width: 100%; height: 5px; background: rgba(255,255,255,0.08); border-radius: 3px; overflow: hidden; margin: 4px 0;">
            <div style="width: ${pct}%; height: 100%; background: #10b981; border-radius: 3px;"></div>
          </div>
          <div style="display: flex; justify-content: space-between; font-size: 9px; color: var(--sim-text-muted); font-family: 'JetBrains Mono', monospace;">
            <span>Saved: ${formatSimMoney(g.current)}</span>
            <span>Target: ${formatSimMoney(g.target)}</span>
          </div>
        </div>
      `;
    }).join('');
  }

  renderSimSvgChart();
}

function renderSimSvgChart() {
  const svg = document.getElementById('sim-analytics-svg');
  if (!svg) return;

  const months = [
    { m: 'Jun', c: 91500, d: 46200 },
    { m: 'Jul', c: 89000, d: 38900 },
    { m: 'Aug', c: 95000, d: 51200 },
    { m: 'Sep', c: 92400, d: 43800 },
    { m: 'Oct', c: 98500, d: 41500 },
    { m: 'Nov', c: 98500, d: 41500 }
  ];

  const w = 340;
  const h = 140;
  const paddingBottom = 22;
  const paddingTop = 12;
  const plotH = h - paddingBottom - paddingTop;
  const step = w / months.length;
  const maxVal = 110000;

  let bars = '';
  months.forEach((item, idx) => {
    const x = step * idx + step / 2;
    const barW = 10;
    const cH = (item.c / maxVal) * plotH;
    const dH = (item.d / maxVal) * plotH;

    // Credit bar
    bars += `<rect x="${x - barW - 1}" y="${paddingTop + plotH - cH}" width="${barW}" height="${cH}" rx="2" fill="#10b981"/>`;
    // Debit bar
    bars += `<rect x="${x + 1}" y="${paddingTop + plotH - dH}" width="${barW}" height="${dH}" rx="2" fill="#f43f5e"/>`;
    // Label
    bars += `<text x="${x}" y="${h - 6}" font-size="9" fill="#94a3b8" text-anchor="middle" font-weight="600">${item.m}</text>`;
  });

  svg.innerHTML = `
    <line x1="0" y1="${paddingTop + plotH}" x2="${w}" y2="${paddingTop + plotH}" stroke="rgba(255,255,255,0.1)" stroke-width="1"/>
    ${bars}
  `;
}

/* ============================================================
   6. RENDER SCHEDULER (BILLS & CHECKBOX TOGGLE)
   ============================================================ */
function renderScheduler() {
  const container = document.getElementById('sim-bills-list');
  const totalDueEl = document.getElementById('sim-sch-total-due');
  const paidEl = document.getElementById('sim-sch-paid');
  const pendingEl = document.getElementById('sim-sch-pending');

  let totalDue = 0;
  let paidAmount = 0;

  simState.bills.forEach(b => {
    totalDue += b.amount;
    if (b.paid) paidAmount += b.amount;
  });

  const pendingAmount = totalDue - paidAmount;

  if (totalDueEl) totalDueEl.textContent = formatSimMoney(totalDue);
  if (paidEl) paidEl.textContent = formatSimMoney(paidAmount);
  if (pendingEl) pendingEl.textContent = formatSimMoney(pendingAmount);

  if (container) {
    container.innerHTML = simState.bills.map(b => {
      const statusBadge = b.paid
        ? `<span style="font-size: 8px; font-weight: 800; padding: 2px 6px; border-radius: 6px; background: rgba(16, 185, 129, 0.15); color: #34d399;">PAID</span>`
        : `<span style="font-size: 8px; font-weight: 800; padding: 2px 6px; border-radius: 6px; background: rgba(245, 158, 11, 0.15); color: #fbbf24;">PENDING</span>`;

      return `
        <div style="background: var(--sim-bg-card-subtle); border: 1px solid var(--sim-border); border-radius: 12px; padding: 10px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; cursor: pointer;" onclick="toggleBillPaid(${b.id})">
          <div style="display: flex; align-items: center; gap: 8px;">
            <input type="checkbox" ${b.paid ? 'checked' : ''} style="cursor: pointer; accent-color: #6366f1;" onclick="event.stopPropagation(); toggleBillPaid(${b.id})">
            <div>
              <div style="font-size: 11px; font-weight: 700; color: var(--sim-text-main); display: flex; align-items: center; gap: 4px;">
                <span>${b.icon}</span>
                <span>${b.title}</span>
              </div>
              <div style="font-size: 9px; color: var(--sim-text-muted);">${b.due}</div>
            </div>
          </div>
          <div style="text-align: right;">
            <div style="font-size: 11px; font-weight: 800; font-family: 'JetBrains Mono', monospace; color: var(--sim-kpi-title);">
              ${formatSimMoney(b.amount)}
            </div>
            ${statusBadge}
          </div>
        </div>
      `;
    }).join('');
  }
}

function toggleBillPaid(billId) {
  const bill = simState.bills.find(b => b.id === billId);
  if (bill) {
    bill.paid = !bill.paid;
    renderScheduler();
  }
}

/* ============================================================
   7. RENDER SETTINGS & APPEARANCE CONTROLS
   ============================================================ */
function renderSettings() {
  // Theme button active state
  const darkBtn = document.getElementById('sim-btn-theme-dark');
  const lightBtn = document.getElementById('sim-btn-theme-light');
  if (darkBtn && lightBtn) {
    if (simState.theme === 'dark') {
      darkBtn.style.background = '#6366f1';
      darkBtn.style.color = '#fff';
      lightBtn.style.background = 'transparent';
      lightBtn.style.color = 'var(--sim-text-muted)';
    } else {
      lightBtn.style.background = '#6366f1';
      lightBtn.style.color = '#fff';
      darkBtn.style.background = 'transparent';
      darkBtn.style.color = 'var(--sim-text-muted)';
    }
  }

  // Currency pills in settings
  document.querySelectorAll('.sim-settings-curr-btn').forEach(btn => {
    btn.classList.remove('active');
    if (btn.getAttribute('data-curr') === simState.currency) {
      btn.classList.add('active');
    }
  });

  // Glass pills in settings
  document.querySelectorAll('.sim-settings-glass-btn').forEach(btn => {
    btn.classList.remove('active');
    if (btn.getAttribute('data-glass') === simState.glassMode) {
      btn.classList.add('active');
    }
  });
}

/* ============================================================
   8. LOCKSCREEN & BIOMETRICS
   ============================================================ */
function setupLockscreen() {
  const lockOverlay = document.getElementById('sim-lock-screen');
  const keypad = document.querySelectorAll('.sim-keypad-key');
  const bioBtn = document.getElementById('sim-lock-bio-btn');

  keypad.forEach(key => {
    key.addEventListener('click', () => {
      const val = key.getAttribute('data-val');
      if (val === 'del') {
        simState.enteredPin = simState.enteredPin.slice(0, -1);
      } else if (simState.enteredPin.length < 4) {
        simState.enteredPin += val;
      }

      updatePinDots();

      if (simState.enteredPin.length === 4) {
        if (simState.enteredPin === '1234') {
          setTimeout(unlockSimulator, 200);
        } else {
          // Shake error animation
          simState.enteredPin = '';
          updatePinDots();
          alert('Incorrect PIN. (Default preview PIN is 1234)');
        }
      }
    });
  });

  if (bioBtn) {
    bioBtn.addEventListener('click', () => {
      bioBtn.textContent = 'Scanning Sensor...';
      setTimeout(() => {
        bioBtn.textContent = 'Touch Fingerprint Sensor';
        unlockSimulator();
      }, 500);
    });
  }
}

function updatePinDots() {
  for (let i = 1; i <= 4; i++) {
    const dot = document.getElementById(`sim-pin-dot-${i}`);
    if (dot) {
      if (i <= simState.enteredPin.length) {
        dot.classList.add('filled');
      } else {
        dot.classList.remove('filled');
      }
    }
  }
}

function lockSimulator() {
  simState.isLocked = true;
  simState.enteredPin = '';
  updatePinDots();
  const lockScreen = document.getElementById('sim-lock-screen');
  if (lockScreen) lockScreen.classList.add('active');
}

function unlockSimulator() {
  simState.isLocked = false;
  simState.enteredPin = '';
  updatePinDots();
  const lockScreen = document.getElementById('sim-lock-screen');
  if (lockScreen) lockScreen.classList.remove('active');
}

/* ============================================================
   9. MODALS (QUICK ADD + BANK SMS INGESTION)
   ============================================================ */
function setupModals() {
  // Modal Closes
  document.querySelectorAll('.sim-modal-close').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.sim-modal-overlay').forEach(m => m.classList.remove('active'));
    });
  });

  // Close when clicking outside modal content (backdrop click)
  document.querySelectorAll('.sim-modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        overlay.classList.remove('active');
      }
    });
  });

  // Open Quick Add
  const openAddBtn = document.getElementById('sim-btn-open-add');
  if (openAddBtn) {
    openAddBtn.addEventListener('click', () => {
      const modal = document.getElementById('sim-modal-add');
      if (modal) modal.classList.add('active');
    });
  }

  // Open SMS Scan
  const openSmsBtn = document.getElementById('sim-btn-open-sms');
  if (openSmsBtn) {
    openSmsBtn.addEventListener('click', () => {
      const modal = document.getElementById('sim-modal-sms');
      if (modal) modal.classList.add('active');
    });
  }

  // Save Manual Entry
  const saveEntryBtn = document.getElementById('sim-btn-save-entry');
  if (saveEntryBtn) {
    saveEntryBtn.addEventListener('click', () => {
      const name = document.getElementById('sim-input-name').value.trim();
      const amount = parseFloat(document.getElementById('sim-input-amount').value);
      const type = document.getElementById('sim-select-type').value;
      const cat = document.getElementById('sim-select-category').value;
      const acc = document.getElementById('sim-input-account').value.trim() || '4821';

      if (!name || isNaN(amount) || amount <= 0) {
        alert('Please enter a valid merchant name and amount.');
        return;
      }

      // Convert from current display currency back to INR base
      const amountInINR = amount / simState.currencyRate;

      const newTx = {
        id: Date.now(),
        name: name,
        amount: Math.round(amountInINR),
        type: type,
        category: cat,
        account: acc,
        date: 'Just Now',
        icon: type === 'CREDIT' ? '💰' : '💸'
      };

      simState.transactions.unshift(newTx);
      document.getElementById('sim-modal-add').classList.remove('active');
      renderAllSimulatorViews();

      // Trigger Island Toast
      triggerDynamicIslandNotification(`Added: ${newTx.name} (${formatSimMoney(newTx.amount)})`);
    });
  }

  // Execute SMS Parsing in Modal
  const execSmsBtn = document.getElementById('sim-btn-execute-sms-modal');
  if (execSmsBtn) {
    execSmsBtn.addEventListener('click', () => {
      const smsText = document.getElementById('sim-sms-modal-input').value;
      parseAndIngestSmsText(smsText);
      document.getElementById('sim-modal-sms').classList.remove('active');
    });
  }

  // SMS Modal Presets
  document.querySelectorAll('.sim-sms-modal-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      const txt = pill.getAttribute('data-sample');
      const input = document.getElementById('sim-sms-modal-input');
      if (input && txt) input.value = txt;
    });
  });
}

function parseAndIngestSmsText(text) {
  let type = 'DEBIT';
  if (/credit|credited|salary|refund|deposited/i.test(text)) {
    type = 'CREDIT';
  }

  let amount = 1200;
  const matchAmount = text.match(/(?:Rs\.?|INR|\₹)\s*([\d,]+(?:\.\d{1,2})?)/i) || text.match(/([\d,]+(?:\.\d{1,2})?)\s*(?:debited|credited|spent)/i);
  if (matchAmount) {
    amount = parseFloat(matchAmount[1].replace(/,/g, ''));
  }

  let merchant = 'Bank Transaction';
  const matchMerchant = text.match(/(?:to|at|info:?)\s+([A-Za-z0-9\s\.\-]{3,24}?)(?=\s+on|\s+via|\s+UPI|\s+Ref|\.|$)/i);
  if (matchMerchant) {
    merchant = matchMerchant[1].trim();
  }

  let account = '4821';
  const matchAcc = text.match(/(?:A\/C|Account|Card|ending|XX|\*\*)\s*([A-Za-z0-9\*]*\d{3,4})/i);
  if (matchAcc) {
    account = matchAcc[1].slice(-4);
  }

  const newTx = {
    id: Date.now(),
    name: merchant,
    amount: amount,
    type: type,
    category: type === 'CREDIT' ? 'Salary' : 'General',
    account: account,
    date: 'Just Now',
    icon: type === 'CREDIT' ? '💰' : '⚡'
  };

  simState.transactions.unshift(newTx);
  renderAllSimulatorViews();
  triggerDynamicIslandNotification(`SMS Ingested: ${merchant} (${formatSimMoney(amount)})`);
}

/* ============================================================
   10. DYNAMIC ISLAND EXPANDING TOAST NOTIFICATION
   ============================================================ */
function triggerDynamicIslandNotification(message) {
  const island = document.getElementById('phone-dynamic-island');
  const toastText = document.getElementById('island-toast-text');
  if (!island || !toastText) return;

  toastText.textContent = message;
  island.classList.add('expanded');

  setTimeout(() => {
    island.classList.remove('expanded');
  }, 4000);
}

/* ============================================================
   11. COMPANION REMOTE CONTROL DECK (OUTSIDE PHONE)
   ============================================================ */
function setupCompanionRemote() {
  // Remote Tab Jump Buttons
  document.querySelectorAll('.remote-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const tab = btn.getAttribute('data-tab');
      switchSimulatorTab(tab);
    });
  });

  // Remote SMS Ingestion Simulator
  const btnTriggerSms = document.getElementById('remote-btn-trigger-sms');
  if (btnTriggerSms) {
    btnTriggerSms.addEventListener('click', () => {
      const samples = [
        "Sent Rs. 650.00 from HDFC Bank A/C **4821 to STARBUCKS COFFEE on 05-Nov-26 via UPI. Bal: Rs. 6,54,150.00.",
        "Dear SBI User, your A/C ending 9012 debited by INR 1,299.00 on 05-Nov-26 at ZOMATO ORDER. Bal: INR 83,021.00.",
        "Cashback bonus of Rs. 250.00 credited to ICICI Bank A/C XX8842 by CRED REWARDS. Avl Bal: Rs. 2,41,150.00."
      ];
      const randomSample = samples[Math.floor(Math.random() * samples.length)];
      parseAndIngestSmsText(randomSample);
    });
  }

  // Remote Lock Phone
  const btnLock = document.getElementById('remote-btn-lock');
  if (btnLock) {
    btnLock.addEventListener('click', lockSimulator);
  }

  // Remote Currency Switcher
  document.querySelectorAll('.remote-curr-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.remote-curr-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const c = btn.getAttribute('data-curr');
      changeSimulatorCurrency(c);
    });
  });

  // Remote Theme Toggle
  const btnToggleTheme = document.getElementById('remote-btn-theme');
  if (btnToggleTheme) {
    btnToggleTheme.addEventListener('click', () => {
      const newTheme = simState.theme === 'dark' ? 'light' : 'dark';
      changeSimulatorTheme(newTheme);
    });
  }

  // Remote Glass Selector
  document.querySelectorAll('.remote-glass-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const g = btn.getAttribute('data-glass');
      changeSimulatorGlass(g);
    });
  });

  // Remote Font Slider
  const fontSlider = document.getElementById('remote-font-slider');
  if (fontSlider) {
    fontSlider.addEventListener('input', (e) => {
      changeSimulatorFontScale(e.target.value);
    });
  }

  // Remote Reset
  const btnReset = document.getElementById('remote-btn-reset');
  if (btnReset) {
    btnReset.addEventListener('click', () => {
      simState = JSON.parse(JSON.stringify(defaultSimulatorData));
      renderAllSimulatorViews();
      triggerDynamicIslandNotification('Simulator Data Restored to Default');
    });
  }
}

/* ============================================================
   12. THEME, CURRENCY, GLASS & FONT SWITCHERS
   ============================================================ */
function changeSimulatorCurrency(code) {
  if (simCurrencyRates[code]) {
    simState.currency = code;
    simState.currencyRate = simCurrencyRates[code].rate;
    simState.currencySymbol = simCurrencyRates[code].symbol;
    renderAllSimulatorViews();
  }
}

function changeSimulatorTheme(themeMode) {
  simState.theme = themeMode;
  const screen = document.getElementById('phone-screen');
  if (screen) {
    screen.classList.remove('theme-dark', 'theme-light');
    screen.classList.add(`theme-${themeMode}`);
  }
  renderSettings();
}

function changeSimulatorGlass(mode) {
  simState.glassMode = mode;
  const screen = document.getElementById('phone-screen');
  if (screen) {
    screen.classList.remove('glass-classic', 'glass-liquid', 'glass-ultra', 'glass-hyper');
    screen.classList.add(`glass-${mode}`);
  }
  renderSettings();
}

function changeSimulatorFontScale(scaleVal) {
  simState.fontScale = scaleVal;
  const screen = document.getElementById('phone-screen');
  const label = document.getElementById('remote-font-label');
  if (screen) {
    const basePx = 13 * (scaleVal / 100);
    screen.style.setProperty('--sim-font-size', `${basePx.toFixed(1)}px`);
  }
  if (label) label.textContent = `${scaleVal}%`;
}

/* ============================================================
   13. MASTER RENDER DISPATCHER
   ============================================================ */
function renderAllSimulatorViews() {
  renderDashboard();
  renderInsights();
  renderScheduler();
  renderSettings();
}

// Transaction Detail Popover
function showTxDetail(id) {
  const tx = simState.transactions.find(t => t.id === id);
  if (!tx) return;

  const conf = confirm(`Delete "${tx.name}" (${formatSimMoney(tx.amount)}) from live demo?`);
  if (conf) {
    simState.transactions = simState.transactions.filter(t => t.id !== id);
    renderAllSimulatorViews();
    triggerDynamicIslandNotification(`Removed ${tx.name}`);
  }
}
