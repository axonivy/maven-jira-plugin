package ch.ivyteam.ivy.jira.release;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ReleaseVersion {

  private static final Pattern VERSION = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)");
  private int major;
  private int minor;
  private int service;

  public ReleaseVersion(int major, int minor, int service) {
    if (major < 0 || minor < 0 || service < 0) {
      throw new IllegalArgumentException("Major, minor and service need to be greater than 0");
    }
    this.major = major;
    this.minor = minor;
    this.service = service;
  }

  public static Optional<ReleaseVersion> parse(String version) {
    if (version == null) {
      return Optional.empty();
    }
    Matcher matcher = VERSION.matcher(version);
    if (matcher.find()) {
      var major = Integer.parseInt(matcher.group(1));
      var minor = Integer.parseInt(matcher.group(2));
      var service = Integer.parseInt(matcher.group(3));
      return Optional.of(new ReleaseVersion(major, minor, service));
    }
    return Optional.empty();
  }

  String toShortString() {
    StringBuilder builder = new StringBuilder();
    builder.append(major).append('.').append(minor);
    if (service != 0) {
      builder.append('.');
      builder.append(service);
    }
    return builder.toString();
  }

  @Override
  public String toString() {
    return major + "." + minor + "." + service;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != ReleaseVersion.class) {
      return false;
    }
    var other = (ReleaseVersion)obj;
    return major == other.major &&
           minor == other.minor &&
           service == other.service;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(major)
        .append(minor)
        .append(service)
        .toHashCode();
  }
}
