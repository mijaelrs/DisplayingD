package it;

import com.atlassian.jira.functest.framework.FuncTestCase;
import org.junit.Test;

public class SchedulerIntegrationTest extends FuncTestCase
{
    @Test
    public void testVersionBeingSet()
    {
        administration.restoreData("SchedulerIntegrationTest.xml");
        navigation.issue().gotoIssue("TEST-1");

        assertions.getViewIssueAssertions().assertFixVersions("Version 1, Version 2");

        tester.clickLink("schedule-web-item-link");

        tester.setFormElement("fixVersions", "-1");
        tester.submit();

        assertions.getViewIssueAssertions().assertFixVersions("None");

        tester.clickLink("schedule-web-item-link");

        tester.setFormElement("fixVersions", "10000");
        tester.submit();

        assertions.getViewIssueAssertions().assertFixVersions("Version 1");
    }
}
