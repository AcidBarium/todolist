import pytest
from appium.webdriver.common.appiumby import AppiumBy
from utils.performance_collector import (
    measure_cold_start_time,
    measure_warm_start_time,
    collect_all_metrics,
)
from config import PACKAGE_NAME, MAIN_ACTIVITY, UI_TIMEOUT


@pytest.mark.performance
class TestLaunchPerformance:

    def test_cold_launch_time(self):
        start_time = measure_cold_start_time(PACKAGE_NAME, MAIN_ACTIVITY)
        assert start_time is not None, "Failed to measure cold start time"
        assert start_time < 5000, f"Cold start too slow: {start_time}ms"

    def test_warm_launch_time(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        start_time = measure_warm_start_time(PACKAGE_NAME, MAIN_ACTIVITY)
        assert start_time is not None, "Failed to measure warm start time"
        assert start_time < 3000, f"Warm start too slow: {start_time}ms"

    def test_launch_memory_and_cpu(self, driver):
        driver.find_element(AppiumBy.ANDROID_UIAUTOMATOR,
            'new UiSelector().text("Tasks")')
        metrics = collect_all_metrics(PACKAGE_NAME, MAIN_ACTIVITY)
        pss = metrics.get("memory_pss_kb")
        cpu = metrics.get("cpu_percent")
        assert pss is not None, "Memory PSS not available"
