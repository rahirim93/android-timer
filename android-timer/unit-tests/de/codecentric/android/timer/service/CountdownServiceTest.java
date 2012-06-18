package de.codecentric.android.timer.service;

import static de.codecentric.android.timer.ServiceStateIteration.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;

import android.os.CountDownTimer;
import android.os.IBinder;

import com.xtremelabs.robolectric.RobolectricTestRunner;

import de.codecentric.android.timer.ActionInState;

@RunWith(RobolectricTestRunner.class)
public class CountdownServiceTest {

	private static final int ONE_SECOND = 1000;

	private CountdownService countdownService;

	@Mock
	private CountDownTimer countDownTimer;

	@Before
	public void before() {
		initMocks(this);
		this.countdownService = new CountdownService();
		this.countdownService.onCreate();
	}

	@Test
	public void shouldProvideBinder() {
		IBinder binder = this.countdownService.onBind(null);
		assertNotNull(binder);
		assertThat(binder, is(instanceOf(CountdownServiceBinder.class)));
	}

	@Test
	public void shouldStopCountdownOnDestroy() {
		CountDownTimer countdownTimer = mock(CountDownTimer.class);
		this.setCountdownTimer(countdownTimer);
		this.countdownService.onDestroy();
		verify(countdownTimer).cancel();
	}

	@Test
	public void shouldStartCountdown() {
		// given a service instance in state WAITING without a running
		// CountdDownTimer object
		this.setServiceState(ServiceState.WAITING);
		assertNull(this.getCountdownTimer());

		// when countdown is started
		this.countdownService.startCountdown(ONE_SECOND);

		// then state is set to COUNTING_DOWN
		assertTrue(this.countdownService.isCountingDown());

		// unfortunately CountdDownTimer object is instantiated inside
		// startCountdown, thus it's quite difficult to verify it has been
		// started. We can verify it has been created, though.
		assertNotNull(this.getCountdownTimer());
	}

	@Test
	public void shouldThrowWhenCountdownIsStartedInWrongState() {
		forAllStatesExcept(ServiceState.WAITING, new ActionInState() {
			@Override
			public void doWithState(ServiceState serviceState) {
				try {
					setServiceState(serviceState);
					countdownService.startCountdown(ONE_SECOND);
					fail("Should have thrown exception");
				} catch (IllegalStateException e) {
					// expected
				}
			}
		});
	}

	@Test
	public void shouldPauseCountdown() {
		// given a service instance in state COUNTING_DOWN with a
		// CountdDownTimer object
		this.setServiceState(ServiceState.COUNTING_DOWN);
		this.setCountdownTimer(this.countDownTimer);

		// when countdown is pause
		this.countdownService.pauseCountdown();

		// then state is set to PAUSED
		assertTrue(this.countdownService.isPaused());

		// and countdown timer is cancelled
		verify(this.countDownTimer).cancel();
	}

	@Test
	public void shouldThrowWhenCountdownIsPausedInWrongState() {
		forAllStatesExcept(ServiceState.COUNTING_DOWN, new ActionInState() {
			@Override
			public void doWithState(ServiceState serviceState) {
				try {
					setServiceState(serviceState);
					countdownService.pauseCountdown();
					fail("Should have thrown exception");
				} catch (IllegalStateException e) {
					// expected
				}
			}
		});
	}

	@Test
	public void shouldContinueCountdown() {
		// given a service instance in state PAUSED
		this.countdownService.startCountdown(ONE_SECOND);
		this.countdownService.pauseCountdown();
		assertTrue(this.countdownService.isPaused());

		// when countdown is continued
		this.countdownService.continueCountdown();

		// then state is set to COUNTING_DOWN again
		assertTrue(this.countdownService.isCountingDown());
	}

	@Test
	public void shouldThrowWhenCountdownIsContinuedInWrongState() {
		forAllStatesExcept(ServiceState.PAUSED, new ActionInState() {
			@Override
			public void doWithState(ServiceState serviceState) {
				try {
					setServiceState(serviceState);
					countdownService.continueCountdown();
					fail("Should have thrown exception");
				} catch (IllegalStateException e) {
					// expected
				}
			}
		});
	}

	// TODO Next to test: stopAlarmSound

	private void setServiceState(ServiceState serviceState) {
		Whitebox.setInternalState(this.countdownService, "serviceState",
				serviceState);
	}

	private void setCountdownTimer(CountDownTimer countdownTimer) {
		Whitebox.setInternalState(this.countdownService, "countdownTimer",
				countdownTimer);
	}

	private CountDownTimer getCountdownTimer() {
		return (CountDownTimer) Whitebox.getInternalState(
				this.countdownService, "countdownTimer");
	}

}