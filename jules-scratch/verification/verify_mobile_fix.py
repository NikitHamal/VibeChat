import asyncio
from playwright.async_api import async_playwright, expect
import os

async def main():
    async with async_playwright() as p:
        # Define a mobile viewport, e.g., iPhone 13
        iphone_13 = p.devices['iPhone 13']

        browser = await p.chromium.launch()
        context = await browser.new_context(**iphone_13)
        page = await context.new_page()

        file_path = os.path.abspath("index.html")
        await page.goto(f"file://{file_path}")

        # --- Navigate to Chat Screen ---
        await page.get_by_placeholder("Username").fill("MobileUser")
        await page.locator("#gender").select_option("female")
        await page.get_by_role("button", name="Start Chat").click()

        # --- Verify Connecting and Chat Screen ---
        await expect(page.locator("#connecting-overlay")).to_be_visible()
        await expect(page.locator("#connecting-overlay")).to_be_hidden(timeout=5000)

        await expect(page.locator("#chat-screen")).to_be_visible()

        # --- Crucial Verification Step ---
        # Check that the send button is visible in the viewport
        await expect(page.locator("#send-btn")).to_be_in_viewport()

        # Take a screenshot to visually confirm the fix
        await page.screenshot(path="jules-scratch/verification/mobile_chat_fix.png")

        await browser.close()

if __name__ == "__main__":
    asyncio.run(main())