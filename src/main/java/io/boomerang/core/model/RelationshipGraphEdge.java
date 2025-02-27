package io.boomerang.core.model;

import lombok.Data;
import org.jgrapht.graph.DefaultEdge;

@Data
public class RelationshipGraphEdge extends DefaultEdge {
  private String label;
  private String role;

  public RelationshipGraphEdge(String label, String role) {
    super(); // Call the constructor of DefaultEdge
    this.label = label;
    this.role = role;
  }

  @Override
  public String toString() {
    return super.toString()
        + "GraphEdge{"
        + "label='"
        + label
        + '\''
        + ", role='"
        + role
        + '\''
        + '}';
  }
}
