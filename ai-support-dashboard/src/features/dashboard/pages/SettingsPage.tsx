export function SettingsPage() {
  return (
    <div className="h-full overflow-auto p-6 space-y-6">
      <div className="mb-8">
        <h1 className="text-2xl font-bold tracking-tight text-foreground">Settings</h1>
        <p className="text-sm text-muted-foreground">Manage your system preferences and account settings.</p>
      </div>
      
      <div className="text-slate-500 p-8 text-sm text-center border-2 border-dashed border-slate-200 rounded-xl bg-white max-w-md mx-auto">
        <span className="block text-2xl mb-2">⚙️</span>
        <div className="font-semibold text-slate-800 mb-1">Settings coming soon</div>
        <div className="text-xs text-slate-500">This module is planned for a future release.</div>
      </div>
    </div>
  );
}
