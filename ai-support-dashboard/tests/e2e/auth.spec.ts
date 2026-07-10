import { test, expect } from '@playwright/test';

test('login and navigate to dashboard for admin', async ({ page }) => {
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
