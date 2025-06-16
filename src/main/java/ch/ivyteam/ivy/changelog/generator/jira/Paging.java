package ch.ivyteam.ivy.changelog.generator.jira;

public class Paging {

  public final String nextPageToken;
  public final int maxResults;
  public final boolean isLast;

  public Paging() {
    this(null, 100, false);
  }

  public Paging(String nextPageToken, int maxResults, boolean isLast) {
    this.nextPageToken = nextPageToken;
    this.maxResults = maxResults;
    this.isLast = isLast;
  }

  public boolean hasNext() {
    return !isLast;
  }
}
