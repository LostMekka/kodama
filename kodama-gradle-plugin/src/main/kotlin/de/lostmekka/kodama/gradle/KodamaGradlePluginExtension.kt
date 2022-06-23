package de.lostmekka.kodama.gradle

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class KodamaGradlePluginExtension @Inject constructor(objects: ObjectFactory) {
  val enabled: Property<Boolean> = objects.property(Boolean::class.javaObjectType).convention(true)
}
