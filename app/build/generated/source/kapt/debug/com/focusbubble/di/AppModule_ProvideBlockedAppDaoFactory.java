package com.focusbubble.di;

import com.focusbubble.data.AppDatabase;
import com.focusbubble.data.dao.BlockedAppDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AppModule_ProvideBlockedAppDaoFactory implements Factory<BlockedAppDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideBlockedAppDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public BlockedAppDao get() {
    return provideBlockedAppDao(databaseProvider.get());
  }

  public static AppModule_ProvideBlockedAppDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideBlockedAppDaoFactory(databaseProvider);
  }

  public static BlockedAppDao provideBlockedAppDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBlockedAppDao(database));
  }
}
