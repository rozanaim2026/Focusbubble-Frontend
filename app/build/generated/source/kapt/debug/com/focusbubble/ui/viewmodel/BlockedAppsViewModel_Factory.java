package com.focusbubble.ui.viewmodel;

import android.content.Context;
import com.focusbubble.data.repository.BlockedAppsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class BlockedAppsViewModel_Factory implements Factory<BlockedAppsViewModel> {
  private final Provider<BlockedAppsRepository> repositoryProvider;

  private final Provider<Context> contextProvider;

  public BlockedAppsViewModel_Factory(Provider<BlockedAppsRepository> repositoryProvider,
      Provider<Context> contextProvider) {
    this.repositoryProvider = repositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public BlockedAppsViewModel get() {
    return newInstance(repositoryProvider.get(), contextProvider.get());
  }

  public static BlockedAppsViewModel_Factory create(
      Provider<BlockedAppsRepository> repositoryProvider, Provider<Context> contextProvider) {
    return new BlockedAppsViewModel_Factory(repositoryProvider, contextProvider);
  }

  public static BlockedAppsViewModel newInstance(BlockedAppsRepository repository,
      Context context) {
    return new BlockedAppsViewModel(repository, context);
  }
}
