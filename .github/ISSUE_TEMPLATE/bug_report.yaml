name: Bug Report
description: Report an issue
title: "[Bug]: "
labels: [bug]
assignees: []
body:
  - type: markdown
    attributes:
      value: |
        Thanks for taking the time to fill out this bug report!
  - type: textarea
    id: what-happened
    attributes:
      label: What happened?
      description: Describe the bug.
      placeholder: Tell us what went wrong!
    validations:
      required: true
  - type: input
    id: plugin-version
    attributes:
      label: Plugin Version
      description: What version of this plugin is your server running? Run the `/terminatorplus` command on your server.
      placeholder: 3.0-BETA
    validations:
      required: true
  - type: input
    id: server-version
    attributes:
      label: Server Version
      description: What software version is your server running?
      placeholder: Spigot 1.16.5
    validations:
      required: true
  - type: textarea
    id: log
    attributes:
      label: Relevant log output
      description: Please copy and paste any relevant log output. This will be automatically formatted into code, so no need for backticks.
      render: shell
    validations:
      required: false
  - type: textarea
    id: context
    attributes:
      label: Additional Context
      description: Add any other useful information regarding your issue.
      placeholder: This only happens if and when...
    validations:
      required: false
  - type: checkboxes
    id: conditions
    attributes:
      label: Checklist
      description: By submitting this issue, you have checked these conditions.
      options:
        - label: My issue does not match any existing issues on this repo
          required: true
