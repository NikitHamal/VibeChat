import asyncio
from playwright.async_api import async_playwright, expect
import os

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch()
        page = await browser.new_page()

        file_path = os.path.abspath("index.html")
        await page.goto(f"file://{file_path}")

        # --- 1. Verify Light Mode Home Screen ---
        await expect(page.locator("body")).to_have_class("light-theme")
        await page.screenshot(path="jules-scratch/verification/01_home_light.png")

        # --- 2. Switch to Dark Mode and Verify ---
        await page.locator("#theme-switcher").click()
        await expect(page.locator("body")).to_have_class("dark-theme")
        await expect(page.locator("#theme-switcher .material-symbols-outlined")).to_have_text("light_mode")
        await page.screenshot(path="jules-scratch/verification/02_home_dark.png")

        # --- 3. Fill form and Start Chat ---
        await page.get_by_placeholder("Username").fill("Jules_v2")
        await page.locator("#gender").select_option("other")
        await page.get_by_placeholder("Age (Optional)").fill("2")
        await page.get_by_role("button", name="Start Chat").click()

        # --- 4. Verify Connecting and Chat Screen (Dark Mode) ---
        await expect(page.locator("#connecting-overlay")).to_be_visible()
        await expect(page.locator("#connecting-overlay")).to_be_hidden(timeout=5000)

        await expect(page.locator("#chat-screen")).to_be_visible()
        await expect(page.locator(".message.system")).to_be_visible()

        await page.get_by_placeholder("Type a message...").fill("This new UI is amazing!")
        await page.get_by_role("button", name="send").click()

        await expect(page.locator(".message.sent")).to_have_text("This new UI is amazing!")
        await expect(page.locator(".message.received")).to_be_visible(timeout=5000)
        await page.screenshot(path="jules-scratch/verification/03_chat_dark.png")

        # --- 5. Switch to Light Mode and Verify Chat Screen ---
        await page.locator("#theme-switcher").click()
        await expect(page.locator("body")).to_have_class("light-theme")
        await expect(page.locator("#theme-switcher .material-symbols-outlined")).to_have_text("dark_mode")
        await page.screenshot(path="jules-scratch/verification/04_chat_light.png")

        await browser.close()

if __name__ == "__main__":
    asyncio.run(main())