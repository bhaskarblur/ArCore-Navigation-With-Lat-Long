// Generated by Dagger (https://dagger.dev).
package com.bhaskarblur.arcorenavigation.di;

import android.content.Context;
import android.hardware.SensorManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class hiltModule_GetSensorManagerFactory implements Factory<SensorManager> {
  private final hiltModule module;

  private final Provider<Context> contextProvider;

  public hiltModule_GetSensorManagerFactory(hiltModule module, Provider<Context> contextProvider) {
    this.module = module;
    this.contextProvider = contextProvider;
  }

  @Override
  public SensorManager get() {
    return getSensorManager(module, contextProvider.get());
  }

  public static hiltModule_GetSensorManagerFactory create(hiltModule module,
      Provider<Context> contextProvider) {
    return new hiltModule_GetSensorManagerFactory(module, contextProvider);
  }

  public static SensorManager getSensorManager(hiltModule instance, Context context) {
    return Preconditions.checkNotNullFromProvides(instance.getSensorManager(context));
  }
}
