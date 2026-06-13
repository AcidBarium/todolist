import time
import pytest
from appium.webdriver.common.appiumby import AppiumBy
from utils.performance_collector import (
    get_gfx_histogram,
    analyze_frames,
    collect_all_metrics,
)
from config import PACKAGE_NAME, MAIN_ACTIVITY, SCROLL_TASK_COUNT


@pytest.mark.performance
class TestScrollPerformance:

    def _add_tasks(self, driver, count):
        for i in range(count):
            driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Add Button").click()
            time.sleep(1)
            edit_texts = driver.find_elements(AppiumBy.CLASS_NAME,
                "android.widget.EditText")
            if len(edit_texts) >= 2:
                edit_texts[0].send_keys(f"ScrollTask {i}")
                edit_texts[1].send_keys(f"Desc {i}")
            driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Add Task").click()
            time.sleep(1)

    def test_scroll_jank(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        self._add_tasks(driver, min(SCROLL_TASK_COUNT, 10))
        time.sleep(1)
        size = driver.get_window_size()
        for _ in range(3):
            driver.swipe(size["width"] // 2, size["height"] * 3 // 4,
                         size["width"] // 2, size["height"] // 4, 300)
            time.sleep(0.5)
        time.sleep(2)
        frames = get_gfx_histogram(PACKAGE_NAME)
        stats = analyze_frames(frames)
        assert stats["total_frames"] > 0, "No frame data captured"

    def test_scroll_memory_stability(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        self._add_tasks(driver, min(SCROLL_TASK_COUNT, 10))
        metrics_before = collect_all_metrics(PACKAGE_NAME, MAIN_ACTIVITY)
        size = driver.get_window_size()
        for _ in range(3):
            driver.swipe(size["width"] // 2, size["height"] * 3 // 4,
                         size["width"] // 2, size["height"] // 4, 300)
            time.sleep(0.3)
        time.sleep(2)
        metrics_after = collect_all_metrics(PACKAGE_NAME, MAIN_ACTIVITY)
        pss_before = metrics_before.get("memory_pss_kb", 0) or 0
        pss_after = metrics_after.get("memory_pss_kb", 0) or 0
        assert pss_after - pss_before < 50000, \
            f"Memory grew too much: {pss_after - pss_before}KB"
