import time
import pytest
from appium.webdriver.common.appiumby import AppiumBy
from utils.performance_collector import collect_all_metrics
from config import PACKAGE_NAME, MAIN_ACTIVITY


@pytest.mark.performance
class TestSearchPerformance:

    def test_search_response_time(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Add Button").click()
        time.sleep(1)
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if len(edit_texts) >= 2:
            edit_texts[0].send_keys("UniqueSearchItem")
            edit_texts[1].send_keys("find me")
        driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Add Task").click()
        time.sleep(2)
        driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Search Tasks").click()
        time.sleep(1)
        t0 = time.time()
        edit_texts = driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")
        if edit_texts:
            edit_texts[0].send_keys("UniqueSearchItem")
        driver.press_keycode(66)
        time.sleep(2)
        resp = (time.time() - t0) * 1000
        metrics = collect_all_metrics(PACKAGE_NAME, MAIN_ACTIVITY)
        assert resp < 3000, f"Search too slow: {resp:.0f}ms"
