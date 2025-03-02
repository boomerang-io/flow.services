package io.boomerang.workflow.model.ref;

import io.boomerang.model.Trigger;

/*
 * This is a fixed trigger model due to the UI. 
 * 
 * TODO: in future you could have a List<Trigger> in Workflow and delete this class
 */
public class WorkflowTrigger {

  private io.boomerang.model.Trigger manual = new io.boomerang.model.Trigger(true);
  private io.boomerang.model.Trigger schedule = new io.boomerang.model.Trigger(false);
  private io.boomerang.model.Trigger webhook = new io.boomerang.model.Trigger(false);
  private io.boomerang.model.Trigger event = new io.boomerang.model.Trigger(false);
  private io.boomerang.model.Trigger github = new io.boomerang.model.Trigger(false);
  
  @Override
  public String toString() {
    return "WorkflowTrigger [manual=" + manual + ", schedule=" + schedule + ", webhook=" + webhook
        + ", event=" + event + ", github=" + github + "]";
  }

  public io.boomerang.model.Trigger getManual() {
    return manual;
  }

  public void setManual(io.boomerang.model.Trigger manual) {
    this.manual = manual;
  }

  public io.boomerang.model.Trigger getSchedule() {
    return schedule;
  }

  public void setSchedule(io.boomerang.model.Trigger schedule) {
    this.schedule = schedule;
  }

  public io.boomerang.model.Trigger getWebhook() {
    return webhook;
  }

  public void setWebhook(io.boomerang.model.Trigger webhook) {
    this.webhook = webhook;
  }

  public io.boomerang.model.Trigger getEvent() {
    return event;
  }

  public void setEvent(io.boomerang.model.Trigger event) {
    this.event = event;
  }

  public io.boomerang.model.Trigger getGithub() {
    return github;
  }

  public void setGithub(Trigger github) {
    this.github = github;
  }
}
