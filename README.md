@echo off

cd /d C:\Test_Automation_App\test-automation-mobile-runner\test-automation-mobile-runner

venv\Scripts\python.exe -m src.entrypoints.precondition_runner --config config/mobile_login_logger.json
