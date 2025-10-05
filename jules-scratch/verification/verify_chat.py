import asyncio
from playwright.async_api import async_playwright, expect
import os

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch()
        page = await browser.new_page()

        # Navigate to the local index.html file
        file_path = os.path.abspath("index.html")
        await page.goto(f"file://{file_path}")

        # --- Home Screen ---
        # Fill in user details
        await page.get_by_placeholder("Username").fill("Jules")
        await page.locator("#gender").select_option("male")
        await page.get_by_placeholder("Age (optional)").fill("30")

        # Click start chat
        await page.get_by_role("button", name="Start Chat").click()

        # --- Chat Screen ---
        # Wait for the chat screen to be visible
        await expect(page.locator("#chat-screen")).to_be_visible()

        # Wait for the "Connecting..." message to disappear
        await expect(page.get_by_text("Connecting...")).to_be_hidden(timeout=5000)

        # Wait for the system message confirming connection
        await expect(page.locator(".message.system")).to_be_visible()

        # Send a message
        await page.get_by_placeholder("Type a message...").fill("Hello, bot!")
        await page.get_by_role("button", name="send").click()

        # Wait for our sent message to appear
        await expect(page.locator(".message.sent")).to_be_visible()

        # Now, wait for the bot's response
        await expect(page.locator(".message.received")).to_be_visible(timeout=5000)

        # Take a screenshot
        await page.screenshot(path="jules-scratch/verification/verification.png")

        await browser.close()

if __name__ == "__main__":
    asyncio.run(main())