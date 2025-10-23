package com.focusbubble.di;

import com.focusbubble.data.repository.ScheduleRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvideScheduleRepositoryFactory implements Factory<ScheduleRepository> {
  @Override
  public ScheduleRepository get() {
    return provideScheduleRepository();
  }

  public static AppModule_ProvideScheduleRepositoryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ScheduleRepository provideScheduleRepository() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideScheduleRepository());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideScheduleRepositoryFactory INSTANCE = new AppModule_ProvideScheduleRepositoryFactory();
  }
}
