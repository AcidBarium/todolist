import time
import pytest
from appium.webdriver.common.appiumby import AppiumBy
from utils.performance_collector import collect_all_metrics
from config import PACKAGE_NAME, MAIN_ACTIVITY


@pytest.mark.performance
class TestTaskCrudPerformance:

    def _add_task(self, driver, title, description):
        driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Add Button").click()
        time.sleep(1.5)
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if len(edit_texts) >= 2:
            edit_texts[0].send_keys(title)
            edit_texts[1].send_keys(description)
        t0 = time.time()
        driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Add Task").click()
        time.sleep(2)
        return (time.time() - t0) * 1000

    def test_add_task_performance(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        resp_time = self._add_task(driver, "Perf Test", "Performance test task")
        metrics = collect_all_metrics(PACKAGE_NAME, MAIN_ACTIVITY)
        assert resp_time < 5000, f"Add task too slow: {resp_time:.0f}ms"

    def test_add_multiple_tasks(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        for i in range(3):
            self._add_task(driver, f"Task {i}", f"Description {i}")
        metrics = collect_all_metrics(PACKAGE_NAME, MAIN_ACTIVITY)
        assert metrics.get("memory_pss_kb") is not None

    def test_edit_task_performance(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        self._add_task(driver, "Edit Me", "Will be edited")
        task = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Edit Me")')
        t0 = time.time()
        task.click()
        time.sleep(2)
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME,
            "android.widget.EditText")
        if edit_texts:
            edit_texts[0].clear()
            edit_texts[0].send_keys("Edited Title")
        driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Update Icon").click()
        time.sleep(2)
        total = (time.time() - t0) * 1000
        assert total < 7000, f"Edit flow too slow: {total:.0f}ms"

    def test_delete_task_performance(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        self._add_task(driver, "Delete Me", "Will be deleted")
        time.sleep(1)
        task = driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Delete Me")')
        size = task.size
        loc = task.location
        start_x = loc["x"] + size["width"] - 10
        start_y = loc["y"] + size["height"] // 2
        end_x = loc["x"] + 10
        t0 = time.time()
        driver.swipe(start_x, start_y, end_x, start_y, 500)
        time.sleep(2.5)
        resp = (time.time() - t0) * 1000
        assert resp < 4000, f"Delete swipe too slow: {resp:.0f}ms"
