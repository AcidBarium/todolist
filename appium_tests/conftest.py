import pytest
from appium import webdriver
from appium.options.android import UiAutomator2Options
from config import APPIUM_HOST, DEVICE_NAME, PLATFORM_NAME, IMPLICIT_WAIT, APK_PATH, PACKAGE_NAME, MAIN_ACTIVITY


@pytest.fixture(scope="function")
def driver():
    options = UiAutomator2Options()
    options.platform_name = PLATFORM_NAME
    options.device_name = DEVICE_NAME
    options.app = APK_PATH
    options.app_package = PACKAGE_NAME
    options.app_activity = MAIN_ACTIVITY
    options.no_reset = False
    options.full_reset = False
    options.auto_grant_permissions = True
    options.new_command_timeout = 60
    options.udid = DEVICE_NAME

    driver = webdriver.Remote(APPIUM_HOST, options=options)
    driver.implicitly_wait(IMPLICIT_WAIT)
    yield driver
    driver.terminate_app(PACKAGE_NAME)
    driver.quit()
