package ch.ivyteam.ivy.changelog.generator.jira;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.settings.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.ivyteam.ivy.changelog.generator.jira.JiraResponse.Issue;

/**
 * jira.username and jira.password must be passed as system property to this
 * test.
 */
class TestJiraService {

  private JiraService testee;

  @BeforeEach
  void setUp() {
    Server server = new Server();
    server.setUsername(System.getProperty("jira.username"));
    server.setPassword(System.getProperty("jira.password"));
    testee = new JiraService("https://1ivy.atlassian.net", server, new SystemStreamLog());
  }

  @Test
  void getIssuesWithFixVersion_71() {
    List<Issue> issues = testee.queryIssues(query("7.1"));
    assertThatXIVY2266isContained(issues);
  }

  @Test
  void getIssuesWithFixVersion_710() {
    List<Issue> issues = testee.queryIssues(query("7.1.0"));
    assertThatXIVY2266isContained(issues);
  }

  private void assertThatXIVY2266isContained(List<Issue> issues) {
    Issue issue = issues.stream().filter(i -> "XIVY-2266".equals(i.key)).findFirst().orElse(null);
    assertThat(issue.getSummary()).isEqualTo("Remove AspectJ");
    assertThat(issue.isUpgradeCritical()).isFalse();
    assertThat(issue.isUpgradeRecommended()).isFalse();
    assertThat(issue.getType()).isEqualTo("Story");
  }

  @Test
  void getIssuesWithFixVersion_703() {
    List<Issue> issues = testee.queryIssues(query("7.0.3"));
    assertThat(issues).hasSize(6);
  }

  @Test
  void getIssuesWithFixVersion_notValidVersion() {
    List<Issue> issues = testee.queryIssues(query("nonExistingVersion"));
    assertThat(issues).isEmpty();
  }

  private static JiraQuery query(String version) {
    StringBuilder builder = new StringBuilder("project = XIVY AND issuetype IN (Story, Improvement, Bug)");
    builder.append(" AND fixVersion = ").append(version);
    return new JiraQuery(builder.toString());
  }
}
