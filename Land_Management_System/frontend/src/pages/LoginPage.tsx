import { Alert, Box, Button, Container, Paper, Stack, TextField, Typography } from "@mui/material";
import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../auth/auth";
import { useAuth } from "../auth/AuthContext";
import Footer from "../components/Footer";
import { publicImageUrl } from "../utils/publicAssets";

const logoUrl = publicImageUrl("đất đai.png");

export default function LoginPage() {
  const nav = useNavigate();
  const { refresh } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const usernameRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);

  // Detect autofill and sync with React state
  useEffect(() => {
    const checkAutofill = () => {
      if (usernameRef.current && usernameRef.current.value !== "" && username === "") {
        setUsername(usernameRef.current.value);
      }
      if (passwordRef.current && passwordRef.current.value !== "" && password === "") {
        setPassword(passwordRef.current.value);
      }
    };

    // Check multiple times with increasing delays to catch autofill
    const timeouts = [
      setTimeout(checkAutofill, 50),
      setTimeout(checkAutofill, 100),
      setTimeout(checkAutofill, 200),
      setTimeout(checkAutofill, 500),
      setTimeout(checkAutofill, 1000),
    ];

    // Also listen for animation events (browsers trigger these on autofill)
    const handleAnimationStart = (e: AnimationEvent) => {
      if (e.animationName === "onAutoFillStart" || e.type === "animationstart") {
        checkAutofill();
      }
    };

    if (usernameRef.current) {
      usernameRef.current.addEventListener("animationstart", handleAnimationStart as any);
    }
    if (passwordRef.current) {
      passwordRef.current.addEventListener("animationstart", handleAnimationStart as any);
    }

    return () => {
      timeouts.forEach(clearTimeout);
      if (usernameRef.current) {
        usernameRef.current.removeEventListener("animationstart", handleAnimationStart as any);
      }
      if (passwordRef.current) {
        passwordRef.current.removeEventListener("animationstart", handleAnimationStart as any);
      }
    };
  }, [username, password]);

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        minHeight: "100vh",
        backgroundImage: `linear-gradient(rgba(246,247,251,0.90), rgba(246,247,251,0.90)), url("${publicImageUrl(
          "trống đồng.png",
        )}")`,
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        backgroundAttachment: "fixed",
      }}
    >
      <style>
        {`
          @keyframes onAutoFillStart {
            from { opacity: 0; }
            to { opacity: 0; }
          }
        `}
      </style>
      <Container maxWidth="sm" sx={{ py: 8, flexGrow: 1, display: "flex", alignItems: "center" }}>
        <Paper elevation={2} sx={{ p: 4, width: "100%" }}>
        <Stack spacing={2}>
          <Box sx={{ display: "flex", alignItems: "center", gap: 1.25 }}>
            <img src={logoUrl} width={48} height={48} alt="Logo" style={{ objectFit: "contain" }} />
            <Box>
              <Typography variant="h5" fontWeight={900} lineHeight={1.1}>
                Đăng nhập hệ thống
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Quản lý hồ sơ đất đai theo mô hình hành chính 3 cấp
              </Typography>
            </Box>
          </Box>

          {err ? <Alert severity="error">{err}</Alert> : null}

          <TextField
            label="Tên đăng nhập"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            fullWidth
            inputRef={usernameRef}
            InputLabelProps={{
              shrink: true,
            }}
            InputProps={{
              sx: {
                fontSize: "1.1rem",
                height: "56px",
                "& input": {
                  fontSize: "1.1rem",
                  padding: "16.5px 14px",
                },
                "&:-webkit-autofill": {
                  animationName: "onAutoFillStart",
                  animationDuration: "0.001s",
                },
              },
            }}
            sx={{
              "& .MuiInputLabel-root": {
                fontSize: "1.1rem",
                "&.MuiInputLabel-shrink": {
                  fontSize: "1rem",
                },
              },
            }}
          />
          <TextField
            label="Mật khẩu"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            type="password"
            fullWidth
            inputRef={passwordRef}
            InputLabelProps={{
              shrink: true,
            }}
            InputProps={{
              sx: {
                fontSize: "1.1rem",
                height: "56px",
                "& input": {
                  fontSize: "1.1rem",
                  padding: "16.5px 14px",
                },
                "&:-webkit-autofill": {
                  animationName: "onAutoFillStart",
                  animationDuration: "0.001s",
                },
              },
            }}
            sx={{
              "& .MuiInputLabel-root": {
                fontSize: "1.1rem",
                "&.MuiInputLabel-shrink": {
                  fontSize: "1rem",
                },
              },
            }}
          />

          <Button
            variant="contained"
            size="large"
            disabled={busy}
            onClick={async () => {
              setBusy(true);
              setErr(null);
              try {
                await login(username.trim(), password);
                await refresh();
                nav("/");
              } catch (e: any) {
  if (e?.response?.status === 401) {
    setErr("Tên đăng nhập hoặc mật khẩu không đúng");
  } else {
    setErr(
      e?.response?.data?.detail ||
      e?.response?.data?.message ||
      "Đăng nhập thất bại"
    );
  }
} finally {
                setBusy(false);
              }
            }}
          >
            Đăng nhập
          </Button>
        </Stack>
      </Paper>
    </Container>
    <Footer />
    </Box>
  );
}


