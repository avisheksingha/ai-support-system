import { test, expect } from '@playwright/test';

test('login and navigate to dashboard for admin', async ({ page }) => {
  // Mock login endpoint
  await page.route('**/auth/login', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        accessToken: 'fake-access-token',
        refreshToken: 'fake-refresh-token'
      })
    });
  });

  // Mock getMe endpoint
  await page.route('**/auth/me', async route => {
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
  await expect(page.locator('h1:has-text("Operations Center")')).toBeVisible();
});
