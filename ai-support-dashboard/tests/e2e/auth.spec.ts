import { test, expect } from '@playwright/test';

test('login and navigate to dashboard for admin', async ({ page }) => {
  // Mock login endpoint (only POST requests)
  await page.route('**/auth/login', async route => {
    if (route.request().method() === 'POST') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'fake-access-token',
          refreshToken: 'fake-refresh-token'
        })
      });
    } else {
      await route.fallback();
    }
  });

  // Mock getMe endpoint (only GET requests)
  await page.route('**/auth/me', async route => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 1,
          email: 'admin@aisupport.com',
          role: 'ADMIN',
          fullName: 'Admin User'
        })
      });
    } else {
      await route.fallback();
    }
  });

  // Mock admin dashboard endpoint (only GET requests)
  await page.route('**/orchestration/dashboard/admin', async route => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          platformOverview: {
            ticketsToday: 12,
            activeTickets: 5,
            resolvedToday: 7,
            aiProcessedToday: 10,
            totalCustomers: 50,
            totalAgents: 10,
            totalAdmins: 2
          },
          aiGovernance: {
            highConfidenceRate: "92%",
            assignmentRate: "88%",
            knowledgeCoverage: "95%",
            averageLatency: "120ms"
          },
          departmentWorkload: { "Support": 5, "Billing": 3 },
          routingOverview: { "Auto-assigned": 8, "Manual": 2 },
          systemHealth: [],
          ragKnowledge: {
            totalArticles: 25,
            embeddedArticles: 25,
            embeddingCoverage: "100%",
            knowledgeCoverage: "95%",
            mostUsedArticle: "Password Reset Guide"
          },
          recentEvents: [],
          myActivity: [],
          platformInfo: {
            platformName: "AI Support Ops",
            platformVersion: "1.0.0",
            buildVersion: "2026.07",
            environment: "development"
          }
        })
      });
    } else {
      await route.fallback();
    }
  });

  await page.goto('/auth/login');
  
  // Fill in login form
  await page.fill('input[placeholder="you@aisupport.com"]', 'admin@aisupport.com');
  await page.fill('input[type="password"]', 'admin123');
  
  // Submit form
  await page.click('button:has-text("Sign in")');
  
  // Should navigate to dashboard eventually
  await expect(page).toHaveURL(/.*dashboard/);
  
  // Verify Dashboard elements
  await expect(page.locator('h1:has-text("Admin User")')).toBeVisible();
});
