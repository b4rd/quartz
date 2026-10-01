/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 * Copyright IBM Corp. 2024, 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.quartz.integrations.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

public class QuartzDatabasePauseAndResumeTest2 extends QuartzDerbyTestSupport {

    @Test
    void testPauseAndResumeTriggers() throws Exception {
        scheduler.pauseAll();
        // qrtz_paused_trigger_grps:
        // TestScheduler,_$_ALL_GROUPS_PAUSED_$_

        JobDetail jobDetail = newJob(HelloJob.class)
                .withIdentity("test_1")
                .build();
        Trigger trigger = newTrigger()
                .withIdentity("test_1", "abc")
                .withSchedule(simpleSchedule())
                .startAt(new Date())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        // qrtz_paused_trigger_grps:
        // TestScheduler,_$_ALL_GROUPS_PAUSED_$_
        // TestScheduler,abc

        // removes the trigger from qrtz_triggers
        scheduler.unscheduleJob(trigger.getKey());

        scheduler.resumeAll();
        // qrtz_paused_trigger_grps:
        // TestScheduler,abc

        scheduler.scheduleJob(jobDetail, trigger);
        Trigger.TriggerState state = scheduler.getTriggerState(TriggerKey.triggerKey("test_1", "abc"));
        assertThat(state, is(Trigger.TriggerState.NORMAL));
        assertThat(state, not(Trigger.TriggerState.PAUSED));
    }
}
