import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { Camera, Monitor, Smartphone, Cpu, Wifi, Activity, Terminal, CheckCircle2, AlertCircle, Download, ExternalLink, Settings, RefreshCcw, WifiOff, Radio } from "lucide-react";

type ConnectionStatus = "IDLE" | "CONNECTING" | "STREAMING" | "ERROR";

export default function App() {
  const [status, setStatus] = useState<ConnectionStatus>("STREAMING");

  // Optional: Cycle through states for demo purposes if needed, 
  // but we'll stick to a primary interactive toggle/display.
  
  const statusConfig = {
    IDLE: { 
      label: "System Idle", 
      color: "zinc", 
      icon: Radio, 
      glow: "bg-zinc-500",
      bg: "bg-zinc-950",
      desc: "Media pipeline ready for initialization."
    },
    CONNECTING: { 
      label: "Negotiating...", 
      color: "blue", 
      icon: RefreshCcw, 
      glow: "bg-blue-500 animate-pulse",
      bg: "bg-blue-500/10",
      desc: "Exchanging RTSP/SDP handshake over UDP."
    },
    STREAMING: { 
      label: "Pipe Active", 
      color: "emerald", 
      icon: Wifi, 
      glow: "bg-emerald-500 shadow-[0_0_12px_rgba(16,185,129,0.5)]",
      bg: "bg-emerald-500/10",
      desc: "Live stream synchronized with OBS instance."
    },
    ERROR: { 
      label: "Link Failed", 
      color: "red", 
      icon: WifiOff, 
      glow: "bg-red-500 animate-bounce",
      bg: "bg-red-500/10",
      desc: "Connection timed out. Check USB/Tethering interface."
    }
  };

  const current = statusConfig[status];

  return (
    <div className="min-h-screen p-6 md:p-10 flex flex-col items-center bg-zinc-950 selection:bg-blue-500/30 font-sans">
      {/* Header */}
      <header className="w-full max-w-6xl flex justify-between items-center mb-10 overflow-hidden">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center shadow-lg shadow-blue-500/20">
            <Smartphone className="text-white w-7 h-7" />
          </div>
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
              StreamCore <span className="text-zinc-500 font-normal text-sm">v1.4.0 (API 28)</span>
            </h1>
            <p className="mono-text">Low Latency Media Pipeline</p>
          </div>
        </div>
        
        <div className="hidden sm:flex gap-4">
          <div className="px-5 py-2.5 bg-zinc-900 border border-zinc-800 rounded-full flex items-center gap-3">
            <span className="relative flex h-2 w-2">
              <span className={`animate-ping absolute inline-flex h-full w-full rounded-full ${current.glow} opacity-75`}></span>
              <span className={`relative inline-flex rounded-full h-2 w-2 ${current.glow}`}></span>
            </span>
            <span className="text-xs font-mono font-medium tracking-tight text-zinc-300 uppercase">{status}</span>
          </div>
          <button 
            onClick={() => {
              const states: ConnectionStatus[] = ["IDLE", "CONNECTING", "STREAMING", "ERROR"];
              const next = states[(states.indexOf(status) + 1) % states.length];
              setStatus(next);
            }}
            className="px-6 py-2.5 bg-blue-600 hover:bg-blue-500 transition-all rounded-full text-xs font-bold uppercase tracking-wider text-white shadow-lg shadow-blue-600/10 active:scale-95"
          >
            Toggle State
          </button>
        </div>
      </header>

      <main className="w-full max-w-6xl grid grid-cols-1 md:grid-cols-12 gap-5 auto-rows-[minmax(120px,auto)] text-[#f4f4f5]">
        {/* Architecture Overview */}
        <motion.div 
          initial={{ opacity: 0, scale: 0.98 }}
          animate={{ opacity: 1, scale: 1 }}
          className="md:col-span-8 md:row-span-4 hardware-card p-8 flex flex-col relative overflow-hidden group"
        >
          <div className="absolute top-0 right-0 p-8 opacity-5">
            <Cpu className="w-32 h-32 text-white" />
          </div>

          <div className="flex items-center justify-between mb-10 z-10">
            <div className="space-y-1">
              <h2 className="text-white text-xl font-bold">Architecture Pipeline</h2>
              <p className="text-[10px] font-mono text-blue-400 uppercase tracking-widest font-bold">Hardware-Accelerated Encoding</p>
            </div>
            <div className="flex gap-3">
              <div className="px-2 py-1 bg-black/60 backdrop-blur-md rounded border border-white/10 text-[10px] font-mono text-zinc-400">EXYNOS 8895</div>
              <div className="px-2 py-1 bg-blue-500/10 rounded border border-blue-500/20 text-[10px] font-mono text-blue-400">AVC/H.264</div>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-10 z-10 flex-grow">
            {[
              { icon: Camera, title: "Camera2 API", desc: "Using direct hardware mapping to the IMX333 sensor. Optimized for zero-wait capturing." },
              { icon: Cpu, title: "MediaCodec", desc: "Leveraging the Exynos hardware encoder for extremely low-latency stream generation." },
              { icon: Wifi, title: "RTSP Protocol", desc: "Native RTSP server running directly on-device. Zero-latency UDP delivery focus." },
              { icon: Activity, title: "Performance", desc: "Target latency < 150ms. No internal buffering, direct stream-out of NAL units." }
            ].map((item, idx) => (
              <div key={idx} className="space-y-3">
                <div className="flex items-center gap-3 text-white/90">
                  <item.icon className="w-5 h-5 text-blue-500" />
                  <span className="font-semibold tracking-tight">{item.title}</span>
                </div>
                <p className="text-zinc-500 text-sm leading-relaxed">{item.desc}</p>
              </div>
            ))}
          </div>

          <div className="mt-8 pt-8 border-t border-zinc-800 flex justify-between items-center">
            <div className="flex gap-4">
              <div className="text-center">
                <p className="text-[10px] text-zinc-500 uppercase font-bold">Target FPS</p>
                <p className="text-sm font-mono text-white">60</p>
              </div>
              <div className="text-center">
                <p className="text-[10px] text-zinc-500 uppercase font-bold">Bitrate</p>
                <p className="text-sm font-mono text-white">4.2 Mbps</p>
              </div>
            </div>
            <div className="flex -space-x-2">
              {[1, 2, 3].map(i => (
                <div key={i} className="w-6 h-6 rounded-full border-2 border-zinc-900 bg-zinc-800 flex items-center justify-center">
                  <div className="w-1 h-1 rounded-full bg-blue-500/50" />
                </div>
              ))}
            </div>
          </div>
        </motion.div>

        {/* Real-time Status Monitor - REPLACES generic asset list or is added as a priority */}
        <motion.div 
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.1 }}
          className="md:col-span-4 md:row-span-2 hardware-card p-0 overflow-hidden relative"
        >
          <div className={`h-full w-full p-8 flex flex-col justify-between transition-colors duration-500 ${current.bg}`}>
            <div>
              <div className="flex items-center justify-between mb-8">
                <h3 className="mono-text tracking-[.25em]">Live Connection Monitor</h3>
                <current.icon className={`w-5 h-5 text-${current.color}-500 transition-all duration-500`} />
              </div>

              <div className="space-y-4">
                <AnimatePresence mode="wait">
                  <motion.div 
                    key={status}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                  >
                    <div className="flex items-center gap-3 mb-2">
                      <div className={`w-3 h-3 rounded-full ${current.glow}`} />
                      <span className="text-2xl font-bold tracking-tight text-white">{current.label}</span>
                    </div>
                    <p className="text-sm text-zinc-500 leading-snug">{current.desc}</p>
                  </motion.div>
                </AnimatePresence>
              </div>
            </div>

            <div className="pt-6 mt-6 border-t border-white/5 space-y-3">
              <div className="flex justify-between items-center text-[10px] font-mono tracking-widest text-zinc-600 uppercase font-bold">
                <span>Network Jitter</span>
                <span className={status === "STREAMING" ? "text-emerald-500" : "text-zinc-600"}>0.12 ms</span>
              </div>
              <div className="flex justify-between items-center text-[10px] font-mono tracking-widest text-zinc-600 uppercase font-bold">
                <span>MTU Size</span>
                <span>1500 (Ethernet)</span>
              </div>
            </div>
          </div>
        </motion.div>

        {/* Deliverables / Assets - Moved down to accommodate Status UI */}
        <motion.div 
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.2 }}
          className="md:col-span-4 md:row-span-1 hardware-card p-6 flex flex-col justify-center border-zinc-800"
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-zinc-950 rounded-lg border border-zinc-800">
                <RefreshCcw className="w-4 h-4 text-blue-500" />
              </div>
              <div>
                <p className="text-xs font-bold text-white uppercase tracking-tight">Sync Assets</p>
                <p className="text-[10px] font-mono text-zinc-600 uppercase">3 New Downloads</p>
              </div>
            </div>
            <Download className="w-4 h-4 text-zinc-700 hover:text-blue-500 cursor-pointer" />
          </div>
        </motion.div>

        {/* RTSP Address Card */}
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15 }}
          className="md:col-span-4 md:row-span-1 hardware-card bg-blue-600 border-none p-6 flex flex-col justify-center relative overflow-hidden group shadow-2xl shadow-blue-600/20"
        >
          <div className="absolute right-[-10px] top-[-10px] opacity-10 group-hover:rotate-12 transition-transform duration-700">
            <Smartphone className="w-32 h-32 text-white" />
          </div>
          <p className="text-[10px] font-bold text-blue-100 uppercase tracking-[0.2em] mb-2 font-mono">Local Network Endpoint</p>
          <div className="flex items-center justify-between gap-4">
            <p className="text-xl font-mono font-bold text-white tracking-tight">rtsp://192.168.1.44:8554</p>
            <div className="p-2.5 bg-white/10 rounded-xl hover:bg-white/20 transition-all cursor-pointer border border-white/5">
              <ExternalLink className="w-5 h-5 text-white" />
            </div>
          </div>
        </motion.div>

        {/* Integration Steps */}
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="md:col-span-6 md:row-span-2 hardware-card p-8"
        >
          <div className="flex items-center gap-4 mb-8">
            <div className="p-2.5 bg-zinc-950 border border-zinc-800 rounded-xl">
              <Terminal className="w-6 h-6 text-white" />
            </div>
            <h3 className="text-base font-bold text-white uppercase tracking-wider">Build via Terminal (Linux)</h3>
          </div>
          
          <div className="space-y-4">
            <div className="bg-black/40 p-4 rounded-xl border border-white/5 font-mono text-[11px] space-y-2">
              <p className="text-zinc-500"># Instalasi di CachyOS / Arch:</p>
              <p className="text-blue-400">sudo pacman -S gradle android-sdk</p>
              <p className="text-zinc-500 mt-2"># Jalankan build:</p>
              <p className="text-blue-400">gradle assembleDebug</p>
            </div>
            <div className="p-4 bg-zinc-950 border border-zinc-800 rounded-2xl flex items-center justify-between">
              <div>
                <p className="text-[10px] font-bold text-zinc-500 uppercase">Output Path</p>
                <p className="text-[11px] text-zinc-400 font-mono">app/build/outputs/apk/debug/</p>
              </div>
              <Download className="w-4 h-4 text-zinc-700" />
            </div>
          </div>
        </motion.div>

        {/* USB / ADB Connection Guide */}
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.22 }}
          className="md:col-span-6 md:row-span-2 hardware-card p-8 bg-blue-600/5 border-blue-500/20"
        >
          <div className="flex items-center gap-4 mb-8">
            <div className="p-2.5 bg-blue-600/10 border border-blue-600/20 rounded-xl">
              <Terminal className="w-6 h-6 text-blue-500" />
            </div>
            <h3 className="text-base font-bold text-white uppercase tracking-wider">USB Connection (ADB)</h3>
          </div>
          
          <div className="space-y-4">
            <p className="text-xs text-zinc-400 leading-relaxed">
              Jika USB Tethering tidak tersedia, gunakan <span className="text-blue-400 font-bold">ADB Port Forwarding</span> untuk stabilitas maksimal:
            </p>
            <div className="bg-black/40 p-4 rounded-xl border border-white/5 font-mono text-[11px] text-blue-400 group">
              <p className="mb-2 text-zinc-600"># Jalankan perintah ini di Linux terminal:</p>
              <p className="group-hover:text-white transition-colors cursor-pointer">adb forward tcp:8554 tcp:8554</p>
            </div>
            <div className="flex items-center gap-3 p-3 bg-white/5 rounded-xl border border-white/5">
              <Activity className="w-4 h-4 text-emerald-500" />
              <p className="text-[10px] text-zinc-400">Gunakan <code className="text-white">rtsp://localhost:8554/live</code> di OBS.</p>
            </div>
          </div>
        </motion.div>
      </main>

      {/* Footer */}
      <footer className="w-full max-w-6xl mt-12 flex flex-col md:flex-row justify-between items-center pt-8 border-t border-zinc-900 gap-6 opacity-60">
        <div className="flex gap-10 uppercase font-mono text-[10px] tracking-widest font-bold">
          <p className="flex items-center gap-2">CPU LOAD: <span className="text-blue-500">24%</span></p>
          <p className="flex items-center gap-2">TEMP: <span className="text-emerald-500">38°C</span></p>
          <p className="flex items-center gap-2">BUFFER: <span className="text-zinc-300">0.1s</span></p>
        </div>
        <div className="flex gap-6 items-center">
          <div className="flex flex-col items-end">
            <span className="mono-text tracking-tighter opacity-100">Android Build System</span>
            <span className="text-xs font-medium text-white">G950F (Galaxy Note 8)</span>
          </div>
          <div className="h-10 w-[1px] bg-zinc-900" />
          <span className="text-[10px] font-mono font-bold text-zinc-400">BUILD: RELEASE.2023.11</span>
        </div>
      </footer>
    </div>
  );
}
