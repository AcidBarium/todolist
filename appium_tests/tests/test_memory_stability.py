import time
import pytest
from appium.webdriver.common.appiumby import AppiumBy
from utils.performance_collector import get_memory_pss, collect_all_metrics
from config import PACKAGE_NAME, MAIN_ACTIVITY, NAVIGATION_LOOPS


@pytest.mark.performance
class TestMemoryStability:

    def test_navigation_memory_leak(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        mem_readings = []
        for i in range(NAVIGATION_LOOPS):
            driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Add Button").click()
            time.sleep(2)
            pss = get_memory_pss(PACKAGE_NAME)
            if pss:
                mem_readings.append(pss)
            driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Back Arrow").click()
            time.sleep(2.5)
            driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
                'new UiSelector().text("Tasks")')
        assert len(mem_readings) > 0, "No memory readings collected"
        max_leak = max(mem_readings) - min(mem_readings)
        assert max_leak < 30000, f"Potential memory leak: {max_leak}KB growth"

    def test_idle_memory_stable(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        time.sleep(2)
        m1 = get_memory_pss(PACKAGE_NAME)
        time.sleep(5)
        m2 = get_memory_pss(PACKAGE_NAME)
        if m1 and m2:
            assert abs(m2 - m1) < 10000, \
                f"Idle memory unstable: {m1} -> {m2}KB"
