# SPDX-License-Identifier: MIT
"""The roll's connectivity probe and its progress meter."""
import io
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import common  # noqa: E402


def test_no_connection_reports_false_without_raising(capsys):
    # 203.0.113.0/24 is TEST-NET-3: reserved, never routable
    assert common.test_internet_connection('203.0.113.1', 9, timeout=1) is False
    assert 'no connection to' in capsys.readouterr().out


def test_the_probe_needs_no_pycurl():
    import inspect
    src = inspect.getsource(common.test_internet_connection)
    assert 'pycurl' not in src.split('"""')[2]


def test_progress_is_silent_when_not_a_terminal(monkeypatch):
    # a roll redirected to a file wrote thousands of \r-terminated meter lines
    # over the output that matters
    buf = io.StringIO()
    monkeypatch.setattr(common, 'stream', buf)
    common.fetch_https_progress(1000, 500, 0, 0)
    assert buf.getvalue() == ''


def test_progress_still_draws_on_a_terminal(monkeypatch):
    class Tty(io.StringIO):
        def isatty(self):
            return True
    buf = Tty()
    monkeypatch.setattr(common, 'stream', buf)
    common.fetch_https_progress(1024000, 512000, 0, 0)
    assert 'Progress:' in buf.getvalue() and '50%' in buf.getvalue()
