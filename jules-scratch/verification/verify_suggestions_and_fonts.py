import asyncio
from playwright.async_api import async_playwright, expect
import os

async def main():
    async with async_playwright() as p:
        # Define a mobile viewport
        iphone_13 = p.devices['iPhone 13']

        browser = await p.chromium.launch()
        context = await browser.new_context(**iphone_13)
        page = await context.new_page()

        file_path = os.path.abspath("index.html")
        await page.goto(f"file://{file_path}")

        # --- Navigate to Chat Screen ---
        await page.get_by_placeholder("Username").fill("FinalCheck")
        await page.locator("#gender").select_option("male")
        await page.get_by_role("button", name="Start Chat").click()
        await expect(page.locator("#connecting-overlay")).to_be_hidden(timeout=5000)

        # --- Verify Message Suggestions ---
        # 1. Check if suggestions are visible
        await expect(page.locator("#message-suggestions")).to_be_visible()
        await expect(page.locator(".suggestion-chip")).to_have_count(6)

        # Take a screenshot to show suggestions are present
        await page.screenshot(path="jules-scratch/verification/01_suggestions_visible.png")

        # 2. Click a suggestion
        suggestion_to_click = page.locator(".suggestion-chip", has_text="ASL?")
        await suggestion_to_click.click()

        # 3. Verify the message was sent and suggestions are now hidden
        await expect(page.locator(".message.sent")).to_have_text("ASL?")
        await expect(page.locator("#message-suggestions")).to_be_hidden()

        # 4. Wait for bot response to ensure the chat is fully rendered
        await expect(page.locator(".message.received")).to_be_visible(timeout=5000)

        # Take a final screenshot to verify font sizes and that suggestions are gone
        await page.screenshot(path="jules-scratch/verification/02_final_mobile_chat_view.png")

        await browser.close()

if __name__ == "__main__":
    asyncio.run(main())