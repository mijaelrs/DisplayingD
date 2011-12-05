package com.example.plugins.tutorial.jira.webwork;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import webwork.action.ActionContext;

import java.util.HashMap;
import java.util.Map;

public class SchedulerWebworkModuleAction extends JiraWebActionSupport
{
    private static final Logger log = LoggerFactory.getLogger(SchedulerWebworkModuleAction.class);

    private final IssueService issueService;
    private final JiraAuthenticationContext authenticationContext;


    private Long[] fixVersions;
    private Long id;
    private IssueService.UpdateValidationResult updateValidationResult;
    private final WebResourceManager webResourceManager;
    private HashMap fieldValuesHolder = null;

    public SchedulerWebworkModuleAction(IssueService issueService, JiraAuthenticationContext authenticationContext, WebResourceManager webResourceManager)
    {
        this.issueService = issueService;
        this.authenticationContext = authenticationContext;
        this.webResourceManager = webResourceManager;

    }

    /**
     *  The validation logic of you action.  Gets called before doExecute().
     *  Use addError() to add form errors for specific fields.
     *  Use addErrorMessage() for adding generic form errors
     */
    protected void doValidation()
    {
        includeResources();

        // Populate the valueHolder from the passed in params (used by the form if there is errors)
        getFixForField().populateFromParams(getFieldValuesHolder(), ActionContext.getParameters());

        // Validation logic
        @SuppressWarnings("unchecked")
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();

        issueInputParameters.setFixVersionIds(fixVersions);
        updateValidationResult = issueService.validateUpdate(authenticationContext.getLoggedInUser(), getId(), issueInputParameters);
        if (!updateValidationResult.isValid())
        {
            this.addErrorCollection(updateValidationResult.getErrorCollection());
        }

    }

    /**
     * The business logic of your form.
     * Only gets called if validation passes.
     *
     * @return the view to display - should usually be "success"
     */
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Business Logic
        final IssueService.IssueResult update = issueService.update(authenticationContext.getLoggedInUser(), updateValidationResult);

        if (!update.isValid())
        {
            return ERROR;
        }

        // We want to redirect back to the view issue page so
	return getRedirect("/browse/" + update.getIssue().getKey());        
    }

    /**
     * The initialization logic of the form.
     * Validation does NOT happen before this.
     *
     * @return the view to display - should usually be "input"
     */
    public String doDefault() throws Exception
    {

        final Issue issue = getIssueObject();
        if (issue == null)
        {
            return INPUT;
        }

        includeResources();

        // populate the values holder from the from the issue (it's initial value)
        getFixForField().populateFromIssue(getFieldValuesHolder(), issue);

        // Check to see if we can update the issue
        @SuppressWarnings("unchecked")
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();

        issueInputParameters.setFixVersionIds(null);

        // This should validate whether the user is able to edit the issue
        IssueService.UpdateValidationResult localResult = issueService.validateUpdate(authenticationContext.getLoggedInUser(), getId(), issueInputParameters);
        if (!localResult.isValid())
        {
            this.addErrorCollection(localResult.getErrorCollection());
        }

        // Initialization logic
        return INPUT;
    }

    /**
     * Used by the action to render the fixVersions field
     *
     * @return the html of the field
     */
    public String getFixForEditHtml()
    {
        final OperationContextImpl operationContext = new OperationContextImpl(IssueOperations.EDIT_ISSUE_OPERATION, getFieldValuesHolder());
        return getFixForField().getEditHtml(null, operationContext, this, getIssueObject(), getDisplayParams());
    }

    private Map<String, Object> getDisplayParams()
    {
        // This will render the field in it's "aui" state.
        final Map<String, Object> displayParams = new HashMap<String, Object>();
        displayParams.put("theme", "aui");
        return displayParams;
    }

    private Map getFieldValuesHolder() {
        if (fieldValuesHolder == null)
        {
            fieldValuesHolder = new HashMap();
        }
        return fieldValuesHolder;
    }

    private void includeResources() {
        webResourceManager.requireResource("jira.webresources:jira-fields");
    }

    private OrderableField getFixForField() {
        return ((OrderableField)getField("fixVersions"));
    }


    /**
     * Used by the decorator
     */
    public GenericValue getProject()
    {
        return getIssue().getProject();
    }

    /**
     * Used by the decorator
     */
    public Issue getIssue()
    {
        return getIssueObject();
    }

    public Issue getIssueObject()
    {
        final IssueService.IssueResult issueResult = issueService.getIssue(authenticationContext.getLoggedInUser(), id);
        if (!issueResult.isValid())
        {
            this.addErrorCollection(issueResult.getErrorCollection());
            return null;
        }

        return  issueResult.getIssue();
    }

    // Getter adn Setters for passing the form params

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long[] getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(Long[] fixVersions) {
        this.fixVersions = fixVersions;
    }
}
