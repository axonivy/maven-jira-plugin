package ch.ivyteam.ivy.changelog.generator.jira;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;

import ch.ivyteam.ivy.changelog.generator.jira.JiraResponse.Issue;
import ch.ivyteam.ivy.jira.JiraClientFactory;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;

public class JiraService {
  private final String serverUri;
  private final Server server;
  private final Log log;

  public JiraService(String serverUri, Server server, Log log) {
    this.serverUri = serverUri;
    this.server = server;
    this.log = log;
  }

  public List<Issue> queryIssues(JiraQuery query) {
    Client client = JiraClientFactory.createClient(server);
    return readIssues(jqlTarget(client, query)).stream()
        .map(i -> {
          i.serverUri = serverUri;
          return i;
        })
        .collect(Collectors.toList());
  }

  private WebTarget jqlTarget(Client client, JiraQuery query) {
    return client
        .target(serverUri)
        .path("rest/api/3/search/jql")
        .queryParam("jql", query.toJql());
  }

  private List<Issue> readIssues(WebTarget target) {
    List<Issue> issues = new ArrayList<>();

    Paging page = new Paging();
    do {
      JiraResponse response = readPaged(target, page);
      issues.addAll(response.issues);
      page = response.nextPage(page);
    } while (page.hasNext());

    return issues;
  }

  private JiraResponse readPaged(WebTarget target, Paging page) {
    WebTarget pagedTarget = target.queryParam("fields", "summary, issuetype, labels, project")
        .queryParam("maxResults", page.maxResults);
    if (page.nextPageToken != null) {
      pagedTarget = pagedTarget.queryParam("nextPageToken", page.nextPageToken);
    }

    log.info("GET: " + pagedTarget.getUri());
    Response response = pagedTarget.request().get();
    if (!Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
      throw new RuntimeException(response.getStatusInfo().getStatusCode() + " "
          + response.getStatusInfo().getReasonPhrase() + " " + response.readEntity(String.class));
    }
    return response.readEntity(JiraResponse.class);
  }

}
