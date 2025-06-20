# Static Weaving Configuration

This document explains the static weaving setup for JPA entities in the Fineract project.

## Overview

Static weaving is a process that enhances JPA entities at build time to improve runtime performance. This is done using the `org.eclipse.persistence.tools.weaving.jpa.StaticWeave` which processes the compiled classes and applies the necessary bytecode transformations.

## Configuration

The static weaving is configured in `static-weaving.gradle` and applied to all Java projects that contain JPA entities.

## How It Works

1. **Compilation**: Java source files are compiled to the standard classes directory (`build/classes/java/main`).
2. **Weaving**: Weaving happens as last step of **compileJava** task, which outputs them to the standard classes directory (`build/classes/java/main`).

## Adding Static Weaving to a Module

1. Add JPA entities to `src/main/java`
2. Ensure there's a `persistence.xml` file in `src/main/resources/jpa/static-weaving/module/[module-name]/`
3. The build will automatically detect and apply static weaving

## Troubleshooting

If you encounter issues with static weaving:

1. Check that the `persistence.xml` file exists in the correct location
2. Verify that the output directories are being created correctly
3. Check the build logs for any weaving-related errors
