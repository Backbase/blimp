# Lint Rules

The linter supports a predefined set of rules that can be configured using the normal Liquibase properties mechanism.

The parameters are in the `blimp.lint` namespace followed by the name of the rule and its specific configurations.

Most of the rules have the options `required`, `equals`, `matches`; this is the order in which every
option is evaluated, e.g. the `author` may not be required, but if it's present it should first equal one of
the items specified by `equals` then it should match the patterns specified by `matches`.

## Supported Rules

### author

Whether the `author` attribute of each changeset is compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.author.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.author.severity` | enum | INFO | the severity level of this rule |
| required | `blimp.lint.author.required` | boolean | false | whether the `author` attribute is required |
| equals | `blimp.lint.author.equals` | list | | if present, the `author` attribute should be equal to one of the specified values |
| matches | `blimp.lint.author.matches` | list | | if present, the `author` attribute should match one of the specified patterns |

### change-log-name

Whether the `change-log-name` compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.change-log-name.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.change-log-name.severity` | enum | INFO | the severity level of this rule |
| equals | `blimp.lint.change-log-name.equals` | list | | if present, the changelog name should be equal to one of the specified values |
| matches | `blimp.lint.change-log-name.matches` | list | `(.+/)?db.changelog-(main|test)\\.(x|y(a)?)ml` | if present, the changelog name should match one of the specified patterns |

### comment

Whether the `comment` element is compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.comment.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.comment.severity` | enum | INFO | the severity level of this rule |
| required | `blimp.lint.comment.required` | boolean | false | whether the `comment` element is required |

### context

Whether the `context` attribute is compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.context.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.context.severity` | enum | INFO | the severity level of this rule |
| required | `blimp.lint.context.required` | boolean | false | whether the `context` attribute is required |
| matches | `blimp.lint.context.matches` | list | | if present, the `context` attribute should match one of the specified patterns |

### foreign-key-deferred

Whether the `deferred` attribute is specified for a foreign key.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.foreign-key-deferred.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.foreign-key-deferred.severity` | enum | INFO | the severity level of this rule |

### foreign-key-name

Whether the foreign key name is compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.foreign-key-name.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.foreign-key-name.severity` | enum | INFO | the severity level of this rule |
| required | `blimp.lint.foreign-key-name.required` | boolean | false | whether a foreign key name is required |
| matches | `blimp.lint.foreign-key-name.matches` | list | | if present, the foreign key name should match one of the specified patterns |

### index-name

Whether the index name is compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.index-name.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.index-name.severity` | enum | INFO | the severity level of this rule |
| required | `blimp.lint.index-name.required` | boolean | false | whether the index name is required |
| matches | `blimp.lint.index-name.matches` | list | | if present, the index name should match one of the specified patterns |

### labels

Whether the `labels` attribute is compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.labels.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.labels.severity` | enum | INFO | the severity level of this rule |
| required | `blimp.lint.labels.required` | boolean | false | whether a `labels` attribute is required |
| matches | `blimp.lint.labels.matches` | list | | if present, the `labels` attribute should match one of the specified patterns |

### one-change

Liquibase recommends one change per changeset.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.one-change.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.one-change.severity` | enum | INFO | the severity level of this rule |

### primary-key-name

Whether the primary key name is compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.primary-key-name.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.primary-key-name.severity` | enum | INFO | the severity level of this rule |
| required | `blimp.lint.primary-key-name.required` | boolean | false | whether a primary key name is required |
| matches | `blimp.lint.primary-key-name.matches` | list | | if present, the primary key name should match one of the specified patterns |

### remarks

Whether the `remarks` attribute is compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.remarks.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.remarks.severity` | enum | INFO | the severity level of this rule |
| required | `blimp.lint.remarks.required` | boolean | false | whether the `remarks` attribute is required |

### unique-constraint-name

Whether the unique constraint name is compliant.

| Option | Property Name | Type | Default | Description |
|-|-|-|-|-|
| enabled | `blimp.lint.unique-constraint-name.enabled` | boolean | true | enable/disable rule |
| severity | `blimp.lint.unique-constraint-name.severity` | enum | INFO | the severity level of this rule |
| required | `blimp.lint.unique-constraint-name.required` | boolean | false | whether a unique constraint name is required |
| matches | `blimp.lint.unique-constraint-name.matches` | list | | if present, the unique constraint name should match one of the specified patterns |

## The rules file

All property names specified above can be aggregated in a file and passed to the linter; it supports the following formats

- yaml
- xml
- properties

**Example**

```yml
blimp:
  lint:
    author:
      severity: ERROR
      required: true
      equals:
        - bob
        - alice
    foreign-key-deferred:
      severity: ERROR
    foreign-key-name:
      enabled: false
    primary-key-name:
      severity: WARN
      required: true
      matches: pk_.+
```
